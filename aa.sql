CREATE DATABASE  IF NOT EXISTS `test` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `test`;
-- MySQL dump 10.13  Distrib 5.6.13, for Win32 (x86)
--
-- Host: 127.0.0.1    Database: test
-- ------------------------------------------------------
-- Server version	5.6.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `active_requests`
--

DROP TABLE IF EXISTS `active_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `active_requests` (
  `requestID` bigint(20) NOT NULL AUTO_INCREMENT,
  `requesterType` varchar(255) NOT NULL,
  `requesterID` bigint(20) NOT NULL,
  `accepterID` bigint(20) NOT NULL,
  `requesttime` bigint(20) NOT NULL,
  `requeststatus` varchar(45) NOT NULL DEFAULT 'pending',
  PRIMARY KEY (`requestID`),
  UNIQUE KEY `requestID_UNIQUE` (`requestID`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `app_registration_keys`
--

DROP TABLE IF EXISTS `app_registration_keys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_registration_keys` (
  `userID` bigint(20) NOT NULL,
  `registrationID` varchar(255) NOT NULL,
  PRIMARY KEY (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `online_passenger`
--

DROP TABLE IF EXISTS `online_passenger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `online_passenger` (
  `userID` bigint(20) NOT NULL,
  `source` varchar(1000) DEFAULT NULL,
  `destination` varchar(1000) DEFAULT NULL,
  `srcgeocode` varchar(1000) DEFAULT NULL,
  `destgeocode` varchar(1000) DEFAULT NULL,
  `isAccepted` varchar(1) DEFAULT 'N',
  `isCompleted` varchar(1) DEFAULT 'N',
  `starttime` bigint(20) unsigned DEFAULT NULL,
  `requestID` varchar(20) DEFAULT '-1',
  PRIMARY KEY (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `online_rider`
--

DROP TABLE IF EXISTS `online_rider`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `online_rider` (
  `userID` bigint(20) NOT NULL,
  `source` varchar(1000) DEFAULT NULL,
  `destination` varchar(1000) DEFAULT NULL,
  `srcgeocode` varchar(1000) DEFAULT NULL,
  `destgeocode` varchar(1000) DEFAULT NULL,
  `isAccepted` varchar(1) DEFAULT 'N',
  `isCompleted` varchar(1) DEFAULT 'N',
  `starttime` bigint(20) DEFAULT NULL,
  `requestID` varchar(20) DEFAULT '-1',
  PRIMARY KEY (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rating_details`
--

DROP TABLE IF EXISTS `rating_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rating_details` (
  `fbuserID_rated` varchar(255) DEFAULT NULL,
  `fbuserID_ratedby` varchar(255) DEFAULT NULL,
  `rating_score` varchar(10) DEFAULT NULL,
  `rating_comment` varchar(10000) DEFAULT NULL,
  `rating_time` bigint(20) DEFAULT NULL,
  `ride_completed` varchar(45) DEFAULT 'Y'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rating_summary`
--

DROP TABLE IF EXISTS `rating_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rating_summary` (
  `fbuserID` varchar(255) NOT NULL,
  `rating_score` varchar(20) DEFAULT NULL,
  `rating_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `request_history`
--

DROP TABLE IF EXISTS `request_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_history` (
  `requestID` bigint(20) DEFAULT NULL,
  `requesterType` varchar(255) DEFAULT NULL,
  `requesterID` bigint(20) DEFAULT NULL,
  `accepterID` bigint(20) DEFAULT NULL,
  `requestStatus` varchar(45) DEFAULT NULL,
  `requesttime` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_details`
--

DROP TABLE IF EXISTS `user_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_details` (
  `userName` varchar(255) NOT NULL,
  `fbuserID` varchar(255) NOT NULL,
  `emailID` varchar(255) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `phone` varchar(20) NOT NULL,
  PRIMARY KEY (`fbuserID`),
  UNIQUE KEY `fbuserID_UNIQUE` (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_dl`
--

DROP TABLE IF EXISTS `user_dl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_dl` (
  `fbuserID` varchar(255) NOT NULL,
  `user_dl` longblob,
  `dl_status` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_pancard`
--

DROP TABLE IF EXISTS `user_pancard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_pancard` (
  `fbuserID` varchar(255) NOT NULL,
  `pan_status` tinyint(4) DEFAULT '0',
  `user_pancard` longblob,
  PRIMARY KEY (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_photo`
--

DROP TABLE IF EXISTS `user_photo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_photo` (
  `fbuserID` varchar(255) NOT NULL,
  `user_photo` longblob,
  PRIMARY KEY (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vehicle_details`
--

DROP TABLE IF EXISTS `vehicle_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vehicle_details` (
  `userID` bigint(20) NOT NULL,
  `vehicletype` varchar(45) NOT NULL,
  `vehiclemodel` varchar(45) NOT NULL,
  `vehiclenumber` varchar(45) NOT NULL,
  `costperkm` varchar(45) NOT NULL,
  PRIMARY KEY (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `verification_status`
--

DROP TABLE IF EXISTS `verification_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `verification_status` (
  `fbuserID` varchar(255) NOT NULL,
  `phone_status` tinyint(4) DEFAULT '0',
  `email_status` tinyint(4) DEFAULT '0',
  `overall_status` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `verify_email`
--

DROP TABLE IF EXISTS `verify_email`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `verify_email` (
  `fbuserID` varchar(255) NOT NULL,
  `emailID` varchar(255) NOT NULL,
  `verification_code` varchar(45) NOT NULL,
  `code_validity` bigint(20) NOT NULL,
  PRIMARY KEY (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `verify_phone`
--

DROP TABLE IF EXISTS `verify_phone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `verify_phone` (
  `fbuserID` varchar(255) NOT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `verification_code` varchar(45) DEFAULT NULL,
  `code_validity` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`fbuserID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'test'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-12-09 19:31:09
