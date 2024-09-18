USE `blog-tracker`;

DROP VIEW IF EXISTS `post_likes_count`;
DROP VIEW IF EXISTS `post_comments_count`;



DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `like`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `category_post`;
DROP TABLE IF EXISTS `tag_post`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `password_reset_token`;
DROP TABLE IF EXISTS `user_follows`;

CREATE TABLE `user` (
                        `id` INT AUTO_INCREMENT NOT NULL,
                        `photo` LONGBLOB,
                        `first_name` VARCHAR(50) NOT NULL,
                        `last_name` VARCHAR(50),
                        `email` VARCHAR(50) NOT NULL,
                        `enabled` BOOLEAN NOT NULL,
                        `username` VARCHAR(50) NOT NULL,
                        `password` VARCHAR(255) NOT NULL,
                        `bio` VARCHAR(1500),
                        `web_push_token` VARCHAR(150),
                        `registered` DATE,
#                         `notification` BOOLEAN,
                        PRIMARY KEY (`id`),
                        UNIQUE(`username`),
                        UNIQUE(`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `role` (
                        `username` VARCHAR(50) NOT NULL,
                        `role` VARCHAR(50) NOT NULL,
                        PRIMARY KEY(`username`, `role`),
                        FOREIGN KEY(`username`) REFERENCES `user`(`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `password_reset_token` (
                                        `id` INT AUTO_INCREMENT PRIMARY KEY,
                                        `token` VARCHAR(255) NOT NULL,
                                        `user_id` INT NOT NULL,
                                        `expiry_date` TIMESTAMP NOT NULL,
                                        FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `post` (
                        `id` INT AUTO_INCREMENT PRIMARY KEY,
                        `user_id` INT NOT NULL,
                        `photo` LONGBLOB,
                        `title` VARCHAR(255) NOT NULL,
                        `status` ENUM('DRAFT', 'PUBLISHED'),
                        `content` TEXT NOT NULL,
                        `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
#                         `new_notification` BOOLEAN,
                        FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `category` (
                            `id` INT NOT NULL UNIQUE AUTO_INCREMENT,
                            `title` VARCHAR(50),
                            PRIMARY KEY(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `category_post` (
                                 `post_id` INT NOT NULL,
                                 `category_id` INT NOT NULL,
                                 PRIMARY KEY(`post_id`, `category_id`),
                                 FOREIGN KEY(`post_id`) REFERENCES `post`(`id`),
                                 FOREIGN KEY(`category_id`) REFERENCES `category`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `tag` (
                       `id` INT NOT NULL UNIQUE AUTO_INCREMENT,
                       `title` VARCHAR(50),
                       PRIMARY KEY(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `tag_post` (
                            `post_id` INT NOT NULL,
                            `tag_id` INT NOT NULL,
                            PRIMARY KEY(`post_id`, `tag_id`),
                            FOREIGN KEY(`post_id`) REFERENCES `post`(`id`),
                            FOREIGN KEY(`tag_id`) REFERENCES `tag`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `comment` (
                           `id` INT AUTO_INCREMENT PRIMARY KEY,
                           `post_id` INT NOT NULL,
                           `user_id` INT NOT NULL,
                           `content` TEXT NOT NULL,
                           `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
#                            `notification` BOOLEAN,
                           FOREIGN KEY (`post_id`) REFERENCES `post`(`id`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `like` (
                        `id` INT AUTO_INCREMENT PRIMARY KEY,
                        `post_id` INT NOT NULL,
                        `user_id` INT NOT NULL,
                        `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
#                         `notification` BOOLEAN,
                        UNIQUE (`post_id`, `user_id`),
                        FOREIGN KEY (`post_id`) REFERENCES `post`(`id`),
                        FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `notification` (
                                `id` INT AUTO_INCREMENT PRIMARY KEY,
                                `recipient_id` INT NOT NULL,
                                `author_id` INT NOT NULL,
                                `post_id` INT,
                                `message` VARCHAR(100) NOT NULL,
                                `redirect_url` VARCHAR(100) NOT NULL,
                                `read` BOOLEAN,
                                `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                `type` enum('L', 'C', 'F', 'P', 'N') not null,
                                `notified` BOOLEAN,
                                FOREIGN KEY(`recipient_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

CREATE TABLE `user_follows` (
#                                 `id` INT NOT NULL,
                                `follower_id` INT NOT NULL,
                                `followed_id` INT NOT NULL,
                                PRIMARY KEY (`follower_id`, `followed_id`),
                                FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`),
                                FOREIGN KEY (`followed_id`) REFERENCES  `user`(`id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;


CREATE VIEW `post_likes_count` AS
SELECT
    `post_id`,
    COUNT(*) AS `like_count`
FROM
    `like`
GROUP BY
    `post_id`;

CREATE VIEW `post_comments_count` AS
SELECT
    `post_id`,
    COUNT(*) AS `comment_count`
FROM
    `comment`
GROUP BY
    `post_id`;
