# create schema
CREATE SCHEMA flashcards;
# set created schema as active
USE flashcards;
# create needed tables
CREATE TABLE `flashcards`
(
    `id`         int       NOT NULL AUTO_INCREMENT,
    `term`       char(255) NOT NULL,
    `definition` char(255) NOT NULL,
    `collection` int       NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `user`
(
    `id`       int          NOT NULL AUTO_INCREMENT,
    `username` char(20)     NOT NULL UNIQUE,
    `pass`     char(255)    NOT NULL,
    `mail`     varchar(320) NOT NULL UNIQUE,
    `role`     tinyint(1)   NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `collections`
(
    `id`       int       NOT NULL AUTO_INCREMENT,
    `creator`  int       NOT NULL,
    `public`   BOOLEAN   NOT NULL,
    `category` char(100) NOT NULL,
    PRIMARY KEY (`id`)
);

ALTER TABLE `flashcards`
    ADD CONSTRAINT `flashcards_fk0` FOREIGN KEY (`collection`) REFERENCES `collections` (`id`);

ALTER TABLE `collections`
    ADD CONSTRAINT `collections_fk0` FOREIGN KEY (`creator`) REFERENCES `user` (`id`);

