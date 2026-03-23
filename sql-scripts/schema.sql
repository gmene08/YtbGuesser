-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema videoguesser_db
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `videoguesser_db` ;

-- -----------------------------------------------------
-- Schema videoguesser_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `videoguesser_db` DEFAULT CHARACTER SET utf8 ;
USE `videoguesser_db` ;

-- -----------------------------------------------------
-- Table `videoguesser_db`.`room`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`room` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`room` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(5) NOT NULL,
  `status` VARCHAR(45) NOT NULL DEFAULT 'WAITING',
  `max_players` INT NOT NULL DEFAULT 2,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`user` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `nickname` VARCHAR(45) NOT NULL,
  `email` VARCHAR(60) NULL,
  `password` VARCHAR(100) NULL,
  `is_guest` TINYINT NOT NULL DEFAULT 1,
  `room_id` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `room_id_idx` (`room_id` ASC) VISIBLE,
  UNIQUE INDEX `nickname_UNIQUE` (`nickname` ASC) VISIBLE,
  CONSTRAINT `fk_user_room`
    FOREIGN KEY (`room_id`)
    REFERENCES `videoguesser_db`.`room` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`match`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`match` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`match` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `number_of_rounds` INT NOT NULL DEFAULT 5,
  `current_round` INT NOT NULL DEFAULT 1,
  `status` VARCHAR(45) NOT NULL DEFAULT 'PREPARING',
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  `room_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `room_id_idx` (`room_id` ASC) VISIBLE,
  CONSTRAINT `fk_match_room`
    FOREIGN KEY (`room_id`)
    REFERENCES `videoguesser_db`.`room` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`category`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`category` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`category` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `slug` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE,
  UNIQUE INDEX `slug_UNIQUE` (`slug` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`video`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`video` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`video` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `youtube_id` VARCHAR(45) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `view_count` BIGINT NOT NULL,
  `channel_name` VARCHAR(100) NULL,
  `thumbnail_url` VARCHAR(255) NULL,
  `category_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `videocol_UNIQUE` (`youtube_id` ASC) VISIBLE,
  INDEX `category_id_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `fk_video_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `videoguesser_db`.`category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`round`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`round` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`round` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `match_id` INT NOT NULL,
  `video_id` INT NOT NULL,
  `round_number` INT NOT NULL DEFAULT 1,
  `status` VARCHAR(45) NOT NULL DEFAULT 'STARTING',
  PRIMARY KEY (`id`),
  INDEX `match_id_idx` (`match_id` ASC) VISIBLE,
  INDEX `video_id_idx` (`video_id` ASC) VISIBLE,
  CONSTRAINT `fk_round_match`
    FOREIGN KEY (`match_id`)
    REFERENCES `videoguesser_db`.`match` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_round_video`
    FOREIGN KEY (`video_id`)
    REFERENCES `videoguesser_db`.`video` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`user_match`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`user_match` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`user_match` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `match_id` INT NOT NULL,
  `final_score` INT NOT NULL DEFAULT 0,
  `final_placement` INT NULL,
  `current_score` INT NOT NULL DEFAULT 0,
  `current_placement` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `user_id_idx` (`user_id` ASC) VISIBLE,
  INDEX `match_id_idx` (`match_id` ASC) VISIBLE,
  CONSTRAINT `fk_user_match_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `videoguesser_db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_match_match`
    FOREIGN KEY (`match_id`)
    REFERENCES `videoguesser_db`.`match` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`user_round`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`user_round` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`user_round` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `round_id` INT NOT NULL,
  `last_guess` BIGINT NULL,
  `points_earned` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `user_id_idx` (`user_id` ASC) VISIBLE,
  INDEX `round_id_idx` (`round_id` ASC) VISIBLE,
  CONSTRAINT `fk_user_round_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `videoguesser_db`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_round_round`
    FOREIGN KEY (`round_id`)
    REFERENCES `videoguesser_db`.`round` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `videoguesser_db`.`match_category`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `videoguesser_db`.`match_category` ;

CREATE TABLE IF NOT EXISTS `videoguesser_db`.`match_category` (
  `match_id` INT NOT NULL,
  `category_id` INT NOT NULL,
  PRIMARY KEY (`match_id`, `category_id`),
  INDEX `match_id_idx` (`match_id` ASC) VISIBLE,
  INDEX `cateory_id_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `fk_match_category_match`
    FOREIGN KEY (`match_id`)
    REFERENCES `videoguesser_db`.`match` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_match_category_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `videoguesser_db`.`category` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
