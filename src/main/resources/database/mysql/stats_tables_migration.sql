
ALTER TABLE o_stat_lastupdated ADD last_log_id bigint(20) NOT NULL DEFAULT '0';

ALTER TABLE `o_stat_daily` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_daily SET signature = MD5(businesspath);
ALTER TABLE `o_stat_daily` ADD INDEX( `signature`, `day`);

ALTER TABLE `o_stat_weekly` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_weekly SET signature = MD5(businesspath);
ALTER TABLE `o_stat_weekly` ADD INDEX( `signature`, `week`);

ALTER TABLE `o_stat_dayofweek` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_dayofweek SET signature = MD5(businesspath);
ALTER TABLE `o_stat_dayofweek` ADD INDEX( `signature`, `day`);

ALTER TABLE `o_stat_hourofday` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_hourofday SET signature = MD5(businesspath);
ALTER TABLE `o_stat_hourofday` ADD INDEX( `signature`, `hour`);

ALTER TABLE `o_stat_homeorg` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_homeorg SET signature = MD5(businesspath);
ALTER TABLE `o_stat_homeorg` ADD INDEX( `signature`, `homeorg`);

ALTER TABLE `o_stat_orgtype` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_orgtype SET signature = MD5(businesspath);
ALTER TABLE `o_stat_orgtype` ADD INDEX( `signature`, `orgtype`);

ALTER TABLE `o_stat_studylevel` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_studylevel SET signature = MD5(businesspath);
ALTER TABLE `o_stat_studylevel` ADD INDEX( `signature`, `studylevel`);

ALTER TABLE `o_stat_studybranch3` ADD `signature` VARCHAR(32) NOT NULL AFTER `businesspath`;
UPDATE o_stat_studybranch3 SET signature = MD5(businesspath);
ALTER TABLE `o_stat_studybranch3` ADD INDEX( `signature`, `studybranch3`);
