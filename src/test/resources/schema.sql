DROP TABLE IF EXISTS `post_view_events`;
DROP TABLE IF EXISTS `post_likes`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `posts`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
`created_at` datetime(6) DEFAULT NULL,
`updated_at` datetime(6) DEFAULT NULL,
`user_id` bigint NOT NULL AUTO_INCREMENT,
`nickname` varchar(10) NOT NULL,
`email` varchar(50) NOT NULL,
`image_url` varchar(255) NOT NULL,
`password` varchar(255) NOT NULL,
PRIMARY KEY (`user_id`),
UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `posts` (
`created_at` datetime(6) DEFAULT NULL,
`post_id` bigint NOT NULL AUTO_INCREMENT,
`updated_at` datetime(6) DEFAULT NULL,
`user_id` bigint DEFAULT NULL,
`view_count` bigint DEFAULT NULL,
`title` varchar(26) NOT NULL,
`image_url` varchar(255) DEFAULT NULL,
`body` longtext NOT NULL,
PRIMARY KEY (`post_id`),
KEY `FK5lidm6cqbc7u4xhqpxm898qme` (`user_id`),
CONSTRAINT `FK5lidm6cqbc7u4xhqpxm898qme` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `comments` (
`comment_id` bigint NOT NULL AUTO_INCREMENT,
`created_at` datetime(6) DEFAULT NULL,
`post_id` bigint DEFAULT NULL,
`updated_at` datetime(6) DEFAULT NULL,
`user_id` bigint DEFAULT NULL,
`body` longtext NOT NULL,
PRIMARY KEY (`comment_id`),
KEY `FK8omq0tc18jd43bu5tjh6jvraq` (`user_id`),
KEY `FKh4c7lvsc298whoyd4w9ta25cr` (`post_id`),
CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ,
CONSTRAINT `FKh4c7lvsc298whoyd4w9ta25cr` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `post_likes` (
`created_at` datetime(6) DEFAULT NULL,
`post_id` bigint DEFAULT NULL,
`post_like_id` bigint NOT NULL AUTO_INCREMENT,
`updated_at` datetime(6) DEFAULT NULL,
`user_id` bigint DEFAULT NULL,
PRIMARY KEY (`post_like_id`),
KEY `FKa5wxsgl4doibhbed9gm7ikie2` (`post_id`),
KEY `FKkgau5n0nlewg6o9lr4yibqgxj` (`user_id`),
CONSTRAINT `FKa5wxsgl4doibhbed9gm7ikie2` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`) ON DELETE CASCADE ,
CONSTRAINT `FKkgau5n0nlewg6o9lr4yibqgxj` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `post_view_events` (
                                    `post_view_event_id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                    `post_id` bigint unsigned NOT NULL,
                                    `created_at` datetime NOT NULL,
                                    `status` enum('PENDING','DONE') NOT NULL DEFAULT 'PENDING',
                                    PRIMARY KEY (`post_view_event_id`),
                                    KEY `idx_pending` (`status`,`post_view_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;




