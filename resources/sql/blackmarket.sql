SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";


/*

 TABLES

 */

/* ITEM STATUS */
create table if not exists blackmarket_item_status
(
    status_id serial,
    descript varchar(255) not null
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;

insert ignore into blackmarket_item_status (status_id, descript)
values
    (1, 'ON_SALE'),
    (2, 'SOLD'),
    (3, 'TAKED'),
    (4, 'TIME_OUT');


/* ITEM DATA */
create table if not exists blackmarket_items
(
    id serial,
    user varchar(36) not null,
    expiration_date timestamp not null,
    status_id bigint unsigned default 1 not null,
    price decimal(7,2) not null, -- million
    notified boolean default false not null,

    foreign key (status_id) references blackmarket_item_status(status_id)
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;

create table if not exists blackmarket_item_data
(
    item_id bigint not null references blackmarket_items(id),
    item_data longtext not null,
    amount tinyint not null,
    material varchar(50) not null
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;

create table if not exists blackmarket_item_content
(
    item_id bigint not null references blackmarket_items(id),
    item_data longtext not null
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;

/* CATEGORIES */
create table blackmarket_category
(
    category varchar(50) not null,
    materials longtext not null
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;




/*

 PROCEDURE

 */

/* SELL ITEM */
CREATE PROCEDURE bm_sell_item (_seller VARCHAR(36), _price DECIMAL(7,2), _item LONGTEXT, _content LONGTEXT, _expiration_date TIMESTAMP, _post_limit SMALLINT)
BEGIN

    DECLARE _item_id BIGINT;

    IF (_post_limit <> -1 and (select count(*) from blackmarket_items where user = _seller and status_id = 1) >= _post_limit) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Post limit';
    END IF;

    INSERT INTO blackmarket_items(user, price, expiration_date) VALUE (_seller, _price, _expiration_date);
    SET _item_id = LAST_INSERT_ID();

    INSERT INTO blackmarket_item_data(item_id, item_data, material, amount) VALUE (_item_id, _item, substring_index(_item, ' ', 1), substring_index(substring_index(_item, ' ', 2), ' ', -1));

    if _content is not null then
        INSERT INTO blackmarket_item_content(item_id, item_data) VALUE (_item_id, _content);
    end if;

END;

/* LOAD ITEMS */
CREATE PROCEDURE bm_load_items (_user VARCHAR(36), _status VARCHAR(255), _category VARCHAR(50), _order_type VARCHAR(20), _invert BOOLEAN, _limit SMALLINT, _offset BIGINT)
BEGIN

    update blackmarket_items set status = 4 where status = 1 and expiration_date < now();

    SET _order_type = (
        CASE _order_type
            WHEN 'amount' THEN 'item_id'
            WHEN 'value' THEN 'price'
            WHEN 'material' THEN 'item_id'
            ELSE 'item_id'
            END);

    SET @invert_str = IF(_invert, ' desc ', ' asc ');

    set @query = concat(
            'select data.item_id, user, data.item_data, content.item_data content, price, status.descript status, expiration_date '
        , 'from blackmarket_items item '
        , 'join blackmarket_item_data data on item.id = data.item_id '
        , 'join blackmarket_item_status status on item.status = status.status_id ');

    IF(_category is not null and _category <> 'all') THEN
        SET @query = concat(@query
            , 'join blackmarket_category category on category.category = ', QUOTE(_category) , ' and data.material in (category.materials) ');
    end if;

    set @query = concat(@query
        , 'left join blackmarket_item_content content on item.id = content.item_id and data.item_data like ', QUOTE('%SHULKER_BOX%'), ' '
        , 'where notified = false and status.descript = ', QUOTE(_status), ' and (', QUOTE(_user), ' is null or user = ', QUOTE(_user), ') '

        , 'order by ', _order_type, @invert_str
        , 'limit ', _limit, ' offset ', _offset, ';');

    PREPARE stmt FROM @query;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

END;

/* LOAD ITEMS SIZE */
CREATE PROCEDURE bm_load_items_size (_user VARCHAR(36), _status VARCHAR(255), _category VARCHAR(50))
BEGIN

    set @query = concat(
            'select count(*) size '
        , 'from blackmarket_items item '
        , 'join blackmarket_item_data data on item.id = data.item_id '
        , 'join blackmarket_item_status status on item.status = status.status_id ');

    IF(_category is not null and _category <> 'all') THEN
        SET @query = concat(@query
            , 'join blackmarket_category category on category.category = ', QUOTE(_category) , ' and data.material in (category.materials) ');
    end if;

    set @query = concat(@query
        , 'where notified = false and status.descript = ', QUOTE(_status), ' and (', QUOTE(_user), ' is null or user = ', QUOTE(_user), ') '
        , 'limit 1;');

    PREPARE stmt FROM @query;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

END;
