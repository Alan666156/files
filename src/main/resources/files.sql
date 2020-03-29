/*
SQLyog Ultimate v11.11 (64 bit)
MySQL - 5.7.17-log : Database - files
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`files` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `files`;

/*Table structure for table `file_config` */

DROP TABLE IF EXISTS `file_config`;

CREATE TABLE `file_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注解id',
  `app_id` varchar(45) DEFAULT NULL COMMENT '业务标识',
  `path` varchar(256) DEFAULT NULL COMMENT '文件目录',
  `status` varchar(50) DEFAULT NULL COMMENT '状态',
  `max_size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `timeout` int(11) DEFAULT NULL COMMENT '超时时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

/*Data for the table `file_config` */

insert  into `file_config`(`id`,`app_id`,`path`,`status`,`max_size`,`timeout`) values (15,'tmp','tmp','0',20971520,30),(16,'product','product','0',20971520,30);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
