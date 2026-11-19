-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: db_geosca
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `autobus`
--

DROP TABLE IF EXISTS `autobus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `autobus` (
  `id_autobus` bigint NOT NULL AUTO_INCREMENT,
  `anio` int DEFAULT NULL,
  `capacidad` int DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `marca` varchar(255) DEFAULT NULL,
  `modelo` varchar(255) DEFAULT NULL,
  `placa` varchar(255) DEFAULT NULL,
  `id_cooperativa` bigint DEFAULT NULL,
  PRIMARY KEY (`id_autobus`),
  KEY `FKooukqr3989af16d6f4tyv682m` (`id_cooperativa`),
  CONSTRAINT `FKooukqr3989af16d6f4tyv682m` FOREIGN KEY (`id_cooperativa`) REFERENCES `cooperativa` (`id_cooperativa`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `autobus`
--

LOCK TABLES `autobus` WRITE;
/*!40000 ALTER TABLE `autobus` DISABLE KEYS */;
INSERT INTO `autobus` VALUES (1,2000,42,'ACTIVO','bus','bus','dfs-888',1),(3,2002,45,'INACTIVO','bus','bus','WER-8458',3);
/*!40000 ALTER TABLE `autobus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conductor`
--

DROP TABLE IF EXISTS `conductor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conductor` (
  `id_conductor` bigint NOT NULL AUTO_INCREMENT,
  `apellidos` varchar(255) DEFAULT NULL,
  `cedula` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `licencia` varchar(255) DEFAULT NULL,
  `nombres` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `fecha_caducidad_licencia` date DEFAULT NULL,
  `id_cooperativa` bigint DEFAULT NULL,
  PRIMARY KEY (`id_conductor`),
  KEY `FK69nqi3huwb4ymt754dnu15eh9` (`id_cooperativa`),
  CONSTRAINT `FK69nqi3huwb4ymt754dnu15eh9` FOREIGN KEY (`id_cooperativa`) REFERENCES `cooperativa` (`id_cooperativa`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conductor`
--

LOCK TABLES `conductor` WRITE;
/*!40000 ALTER TABLE `conductor` DISABLE KEYS */;
INSERT INTO `conductor` VALUES (1,'Paez','0550372825','camilo@gmail.com','ACTIVO','Profesional','Fernando','0335448575','2027-04-24',1);
/*!40000 ALTER TABLE `conductor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cooperativa`
--

DROP TABLE IF EXISTS `cooperativa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cooperativa` (
  `id_cooperativa` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `ruc` varchar(255) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_cooperativa`),
  UNIQUE KEY `ruc` (`ruc`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cooperativa`
--

LOCK TABLES `cooperativa` WRITE;
/*!40000 ALTER TABLE `cooperativa` DISABLE KEYS */;
INSERT INTO `cooperativa` VALUES (1,'Tanicuchi','1234567891234','latacunga','0987654321','tanicuchi@geosca.com','ACTIVA'),(2,'Guaytacama','0967671615096','latacunga','0967671615','guaytacama@geosca.com','ACTIVA'),(3,'Belisario Quevedo','2547825496158','latacunga','0254896475','belisario@geosca.com','ACTIVA');
/*!40000 ALTER TABLE `cooperativa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parroquia`
--

DROP TABLE IF EXISTS `parroquia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parroquia` (
  `id_parroquia` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `canton` varchar(255) DEFAULT NULL,
  `provincia` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_parroquia`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parroquia`
--

LOCK TABLES `parroquia` WRITE;
/*!40000 ALTER TABLE `parroquia` DISABLE KEYS */;
INSERT INTO `parroquia` VALUES (1,'La Matriz','ACTIVA','Latacunga','Cotopaxi'),(2,'Eloy Alfaro','ACTIVA','Latacunga','Cotopaxi'),(3,'Ignacio Flores','ACTIVA','Latacunga','Cotopaxi'),(4,'Juan Montalvo','ACTIVA','Latacunga','Cotopaxi'),(5,'San Buenaventura','ACTIVA','Latacunga','Cotopaxi'),(6,'Aláquez','ACTIVA','Latacunga','Cotopaxi'),(7,'Belisario Quevedo','ACTIVA','Latacunga','Cotopaxi'),(8,'Guaytacama','ACTIVA','Latacunga','Cotopaxi'),(9,'Joseguango Bajo','ACTIVA','Latacunga','Cotopaxi'),(10,'Mulaló','ACTIVA','Latacunga','Cotopaxi'),(11,'Poaló','ACTIVA','Latacunga','Cotopaxi'),(12,'Tanicuchí','ACTIVA','Latacunga','Cotopaxi'),(13,'Toacaso','ACTIVA','Latacunga','Cotopaxi'),(14,'11 de Noviembre','ACTIVA','Latacunga','Cotopaxi'),(15,'San Juan de Pastocalle','ACTIVA','Latacunga','Cotopaxi');
/*!40000 ALTER TABLE `parroquia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permiso`
--

DROP TABLE IF EXISTS `permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permiso` (
  `id_permiso` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id_permiso`),
  UNIQUE KEY `UKnwe6lkk7x7sbw94xcmbwgvycu` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permiso`
--

LOCK TABLES `permiso` WRITE;
/*!40000 ALTER TABLE `permiso` DISABLE KEYS */;
INSERT INTO `permiso` VALUES (1,'Administrar roles y permisos','GESTIONAR_ROLES'),(2,'Crear, editar y administrar usuarios','GESTIONAR_USUARIOS'),(3,'Administrar autobuses','GESTIONAR_AUTOBUSES'),(4,'Administrar parroquias','GESTIONAR_PARROQUIAS'),(5,'Administrar rutas','GESTIONAR_RUTAS'),(6,'Administrar conductores','GESTIONAR_CONDUCTORES'),(7,'Administrar cooperativas','GESTIONAR_COOPERATIVAS'),(8,'Consultar módulos de transporte','CONSULTAR_TRANSPORTE');
/*!40000 ALTER TABLE `permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id_rol` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_rol`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'ADMIN_GENERAL','Administrador General'),(2,'ADMIN_COOPERATIVA','Administrador Cooperativa'),(3,'OPERADOR','Operador'),(4,'BOLETERIA','Personal de boletería');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_permiso`
--

DROP TABLE IF EXISTS `rol_permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_permiso` (
  `id_rol` bigint NOT NULL,
  `id_permiso` bigint NOT NULL,
  PRIMARY KEY (`id_rol`,`id_permiso`),
  KEY `FKrhxhgw05bdvokfrpppumlfh5d` (`id_permiso`),
  CONSTRAINT `FKrhxhgw05bdvokfrpppumlfh5d` FOREIGN KEY (`id_permiso`) REFERENCES `permiso` (`id_permiso`),
  CONSTRAINT `FKsxc3d8lmtj7em6n8j0wl4jwco` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permiso`
--

LOCK TABLES `rol_permiso` WRITE;
/*!40000 ALTER TABLE `rol_permiso` DISABLE KEYS */;
INSERT INTO `rol_permiso` VALUES (1,1),(1,2),(1,3),(2,3),(1,4),(1,5),(2,5),(1,6),(2,6),(1,7),(1,8),(2,8);
/*!40000 ALTER TABLE `rol_permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ruta`
--

DROP TABLE IF EXISTS `ruta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ruta` (
  `id_ruta` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `destino` varchar(255) DEFAULT NULL,
  `distancia_km` double DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `origen` varchar(255) DEFAULT NULL,
  `paradas_intermedias` varchar(255) DEFAULT NULL,
  `id_cooperativa` bigint DEFAULT NULL,
  `id_parroquia_destino` bigint DEFAULT NULL,
  `id_parroquia_origen` bigint DEFAULT NULL,
  PRIMARY KEY (`id_ruta`),
  KEY `FKk70bf4042aae52aucjqd9dtdg` (`id_cooperativa`),
  KEY `FKd0w1jsdd0xarr482pp6nqt8ta` (`id_parroquia_destino`),
  KEY `FKdj3aa89lbr3f5i8kgoh6kfls4` (`id_parroquia_origen`),
  CONSTRAINT `FKd0w1jsdd0xarr482pp6nqt8ta` FOREIGN KEY (`id_parroquia_destino`) REFERENCES `parroquia` (`id_parroquia`),
  CONSTRAINT `FKdj3aa89lbr3f5i8kgoh6kfls4` FOREIGN KEY (`id_parroquia_origen`) REFERENCES `parroquia` (`id_parroquia`),
  CONSTRAINT `FKk70bf4042aae52aucjqd9dtdg` FOREIGN KEY (`id_cooperativa`) REFERENCES `cooperativa` (`id_cooperativa`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ruta`
--

LOCK TABLES `ruta` WRITE;
/*!40000 ALTER TABLE `ruta` DISABLE KEYS */;
/*!40000 ALTER TABLE `ruta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ruta_parada`
--

DROP TABLE IF EXISTS `ruta_parada`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ruta_parada` (
  `id_ruta` bigint NOT NULL,
  `id_parroquia` bigint NOT NULL,
  PRIMARY KEY (`id_ruta`,`id_parroquia`),
  KEY `FK2rlmorffxgdkexy7ruqi12kav` (`id_parroquia`),
  CONSTRAINT `FK2rlmorffxgdkexy7ruqi12kav` FOREIGN KEY (`id_parroquia`) REFERENCES `parroquia` (`id_parroquia`),
  CONSTRAINT `FKl58bg15ipt86jf2aqpb94v2bf` FOREIGN KEY (`id_ruta`) REFERENCES `ruta` (`id_ruta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ruta_parada`
--

LOCK TABLES `ruta_parada` WRITE;
/*!40000 ALTER TABLE `ruta_parada` DISABLE KEYS */;
/*!40000 ALTER TABLE `ruta_parada` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `estado` tinyint(1) DEFAULT '1',
  `id_rol` bigint NOT NULL,
  `id_cooperativa` bigint DEFAULT NULL,
  `password1` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `correo` (`correo`),
  KEY `fk_usuario_rol` (`id_rol`),
  KEY `fk_usuario_cooperativa` (`id_cooperativa`),
  CONSTRAINT `fk_usuario_cooperativa` FOREIGN KEY (`id_cooperativa`) REFERENCES `cooperativa` (`id_cooperativa`),
  CONSTRAINT `fk_usuario_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Administrador','Sistema','admin@geosca.com','admin','$2a$10$xdBKFU4cQ/6Ne/w6bVTLiuTTTPX7FRTwnTROzY81F3d3U4cPUiTHq',1,1,NULL,NULL),(5,'Felipe','Andrade','felipe@geosca.com','felipe','$2a$10$94NQAutQxxblL0eIptYzEeIEyxbpceZeS4EEnlrm4Jy35Kkf.oNSG',1,2,1,NULL),(8,'Ramiro','Hernandez','ramiro@geosca.com','ramiro','$2a$10$iov4OqnIKU.Kua35UNqopeI9A1.5KouVtaWtFp6MrSKMVcfIN08CG',1,3,NULL,NULL),(11,'Raul','Hernandez','raul@geosca.com','raul','$2a$10$21pZ1P038lUYMaSEIG/yiO9yPqJZoXX5QLZV0quT/4UMyw55UaqLW',1,2,3,NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-18 23:33:40
