/*
Navicat MySQL Data Transfer

Source Server         : 本地
Source Server Version : 50528
Source Host           : 127.0.0.1:3306
Source Database       : restful_crud

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2018-03-05 10:41:40
*/

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for department
-- ----------------------------
CREATE TABLE IF NOT EXISTS `payment`
(
    `id`     bigint(20) NOT NULL AUTO_INCREMENT,
    `serial` varchar(255) DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

