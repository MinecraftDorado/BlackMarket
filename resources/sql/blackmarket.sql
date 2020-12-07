SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

CREATE TABLE IF NOT EXISTS `blackitems` (
  `id` INT(255) NOT NULL AUTO_INCREMENT,
  `owner` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `date` VARCHAR(255) NOT NULL,
  `status` VARCHAR(255) NOT NULL,
  `item` VARCHAR(255) NOT NULL,
  `notified` BOOLEAN NOT NULL default false,
  PRIMARY KEY (`id`),
  KEY (`owner`)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =latin1;

ALTER TABLE `blackitems` ADD `notified` BOOLEAN NOT NULL default false ;