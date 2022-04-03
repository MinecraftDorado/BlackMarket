SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";;


/*

 TABLES

 */

/* ITEM STATUS */
CREATE TABLE IF NOT EXISTS blackmarket_item_status
(
    status_id serial,
    descript varchar(255) not null
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;;

INSERT IGNORE INTO blackmarket_item_status (status_id, descript)
VALUES
    (1, 'ON_SALE'),
    (2, 'SOLD'),
    (3, 'TAKED'),
    (4, 'TIME_OUT');;


/* ITEM DATA */
CREATE TABLE IF NOT EXISTS blackmarket_items
(
    id serial,
    user varchar(36) not null,
    expiration_date timestamp not null,
    status_id bigint unsigned default 1 not null,
    price decimal(7,2) not null, /* million */
    notified boolean default false not null,

    foreign key (status_id) references blackmarket_item_status(status_id),
    index (status_id)
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;;

CREATE TABLE IF NOT EXISTS blackmarket_item_data
(
    item_id bigint not null references blackmarket_items(id),
    item_data longtext not null,
    amount tinyint not null,
    material varchar(50) not null,

    index (material)
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;;

CREATE TABLE IF NOT EXISTS blackmarket_item_content
(
    item_id bigint not null references blackmarket_items(id),
    item_data longtext not null,

    index (item_id)
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;;

/* CATEGORIES */
CREATE TABLE IF NOT EXISTS blackmarket_category
(
    category varchar(50) not null,
    materials longtext not null,

    index (category)
) ENGINE =InnoDB DEFAULT CHARSET = utf8mb4;;




/*

 PROCEDURE

 */

/* SELL ITEM */
CREATE PROCEDURE bm_sell_item (_seller VARCHAR(36), _price DECIMAL(7,2), _item LONGTEXT, _content LONGTEXT, _expiration_date TIMESTAMP, _post_limit SMALLINT)
BEGIN

    DECLARE _item_id BIGINT;

    IF (_post_limit <> -1 AND (SELECT count(*) FROM blackmarket_items WHERE user = _seller AND status_id = 1) >= _post_limit) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Post limit';
    END IF;

    INSERT INTO blackmarket_items(user, price, expiration_date) VALUE (_seller, _price, _expiration_date);
    SET _item_id = LAST_INSERT_ID();

    INSERT INTO blackmarket_item_data(item_id, item_data, material, amount) VALUE (_item_id, _item, substring_index(_item, ' ', 1), substring_index(substring_index(_item, ' ', 2), ' ', -1));

    IF _content IS NOT NULL THEN
        INSERT INTO blackmarket_item_content(item_id, item_data) VALUE (_item_id, _content);
    END IF;

END;;

/* LOAD ITEMS */
CREATE PROCEDURE bm_load_items (_user VARCHAR(36), _status VARCHAR(255), _category VARCHAR(50), _order_type VARCHAR(20), _invert BOOLEAN, _limit SMALLINT, _offset BIGINT)
BEGIN

    UPDATE blackmarket_items SET status_id = 4 WHERE status_id = 1 AND expiration_date < now();

    SET _order_type = (
        CASE _order_type
            WHEN 'amount' THEN 'amount'
            WHEN 'value' THEN 'price'
            WHEN 'material' THEN 'data.material'
            ELSE 'item_id'
            END);

    SET @invert_str = IF(_invert, ' desc ', ' asc ');

    SET @query = concat(
            'select data.item_id, user, data.item_data, content.item_data content, price, status.descript status, expiration_date '
        , 'from blackmarket_items item '
        , 'join blackmarket_item_data data on item.id = data.item_id '
        , 'join blackmarket_item_status status on item.status_id = status.status_id ');

    IF(_category IS NOT NULL AND _category <> 'all') THEN
        SET @query = concat(@query
            , 'join blackmarket_category category on category.category = ', QUOTE(_category) , ' and data.material in (category.materials) ');
    END IF;

    SET @query = concat(@query
        , 'left join blackmarket_item_content content on item.id = content.item_id and data.item_data like ', QUOTE('%SHULKER_BOX%'), ' '
        , 'where notified = false and status.descript = ', QUOTE(_status), ' and (', QUOTE(_user), ' is null or user = ', QUOTE(_user), ') '

        , 'order by ', _order_type, @invert_str
        , 'limit ', _limit, ' offset ', _offset, ';');

    PREPARE stmt FROM @query;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

END;;

/* LOAD ITEMS SIZE */
CREATE PROCEDURE bm_load_items_size (_user VARCHAR(36), _status VARCHAR(255), _category VARCHAR(50))
BEGIN

    SET @query = concat(
            'select count(*) size '
        , 'from blackmarket_items item '
        , 'join blackmarket_item_data data on item.id = data.item_id '
        , 'join blackmarket_item_status status on item.status_id = status.status_id ');

    IF(_category IS NOT NULL AND _category <> 'all') THEN
        SET @query = concat(@query
            , 'join blackmarket_category category on category.category = ', QUOTE(_category) , ' and data.material in (category.materials) ');
    END IF;

    SET @query = concat(@query
        , 'where notified = false and status.descript = ', QUOTE(_status), ' and (', QUOTE(_user), ' is null or user = ', QUOTE(_user), ') '
        , 'limit 1;');

    PREPARE stmt FROM @query;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

END;;
