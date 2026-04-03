-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema videoguesser_db
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema videoguesser_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `videoguesser_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `videoguesser_db` ;

-- -----------------------------------------------------
-- Table `videoguesser_db`.`category`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`category` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `slug` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE,
  UNIQUE INDEX `slug_UNIQUE` (`slug` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nickname` VARCHAR(45) NOT NULL,
  `email` VARCHAR(60) NULL DEFAULT NULL,
  `password` VARCHAR(100) NULL DEFAULT NULL,
  `is_guest` BIT(1) NOT NULL DEFAULT b'1',
  `room_id` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `nickname_UNIQUE` (`nickname` ASC) VISIBLE,
  INDEX `room_id_idx` (`room_id` ASC) VISIBLE,
  CONSTRAINT `fk_user_room`
    FOREIGN KEY (`room_id`)
    REFERENCES `videoguesser_db`.`room` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`room`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`room` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(5) NOT NULL,
  `status` VARCHAR(45) NOT NULL DEFAULT 'WAITING',
  `max_players` INT NOT NULL DEFAULT '2',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `owner_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC) VISIBLE,
  UNIQUE INDEX `owner_id_UNIQUE` (`owner_id` ASC) VISIBLE,
  CONSTRAINT `fk_room_user`
    FOREIGN KEY (`owner_id`)
    REFERENCES `videoguesser_db`.`user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 8
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`matches`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`matches` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `number_of_rounds` INT NOT NULL DEFAULT '5',
  `current_round` INT NOT NULL DEFAULT '1',
  `status` VARCHAR(45) NOT NULL DEFAULT 'PREPARING',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `room_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `room_id_idx` (`room_id` ASC) VISIBLE,
  CONSTRAINT `fk_match_room`
    FOREIGN KEY (`room_id`)
    REFERENCES `videoguesser_db`.`room` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`match_category`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`match_category` (
  `match_id` INT NOT NULL,
  `category_id` INT NOT NULL,
  PRIMARY KEY (`match_id`, `category_id`),
  INDEX `match_id_idx` (`match_id` ASC) VISIBLE,
  INDEX `cateory_id_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `fk_match_category_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `videoguesser_db`.`category` (`id`),
  CONSTRAINT `fk_match_category_match`
    FOREIGN KEY (`match_id`)
    REFERENCES `videoguesser_db`.`matches` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`video`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`video` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `youtube_id` VARCHAR(45) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `view_count` BIGINT NOT NULL,
  `channel_name` VARCHAR(100) NULL DEFAULT NULL,
  `thumbnail_url` VARCHAR(255) NULL DEFAULT NULL,
  `category_id` INT NOT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `videocol_UNIQUE` (`youtube_id` ASC) VISIBLE,
  INDEX `category_id_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `fk_video_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `videoguesser_db`.`category` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 865
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`round`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`round` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `match_id` INT NOT NULL,
  `video_id` INT NOT NULL,
  `round_number` INT NOT NULL DEFAULT '1',
  `status` VARCHAR(45) NOT NULL DEFAULT 'STARTING',
  PRIMARY KEY (`id`),
  INDEX `match_id_idx` (`match_id` ASC) VISIBLE,
  INDEX `video_id_idx` (`video_id` ASC) VISIBLE,
  CONSTRAINT `fk_round_match`
    FOREIGN KEY (`match_id`)
    REFERENCES `videoguesser_db`.`matches` (`id`),
  CONSTRAINT `fk_round_video`
    FOREIGN KEY (`video_id`)
    REFERENCES `videoguesser_db`.`video` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`user_match`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`user_match` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `match_id` INT NOT NULL,
  `final_score` INT NOT NULL DEFAULT '0',
  `final_placement` INT NULL DEFAULT NULL,
  `current_score` INT NOT NULL DEFAULT '0',
  `current_placement` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `user_id_idx` (`user_id` ASC) VISIBLE,
  INDEX `match_id_idx` (`match_id` ASC) VISIBLE,
  CONSTRAINT `fk_user_match_match`
    FOREIGN KEY (`match_id`)
    REFERENCES `videoguesser_db`.`matches` (`id`),
  CONSTRAINT `fk_user_match_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `videoguesser_db`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`user_round`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `videoguesser_db`.`user_round` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `round_id` INT NOT NULL,
  `last_guess` BIGINT NULL DEFAULT NULL,
  `points_earned` INT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `user_id_idx` (`user_id` ASC) VISIBLE,
  INDEX `round_id_idx` (`round_id` ASC) VISIBLE,
  CONSTRAINT `fk_user_round_round`
    FOREIGN KEY (`round_id`)
    REFERENCES `videoguesser_db`.`round` (`id`),
  CONSTRAINT `fk_user_round_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `videoguesser_db`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
