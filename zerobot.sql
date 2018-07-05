CREATE TABLE IF NOT EXISTS `aliases` (
  `alias` varchar(255) NOT NULL,
  `command` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (`alias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `messages` (
  `id` varchar(64) NOT NULL,
  `user_id` varchar(64) NOT NULL,
  `content` text NOT NULL,
  `timestamp` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_messages_users` (`user_id`),
  CONSTRAINT `FK_messages_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `options` (
  `key` varchar(255) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `users` (
  `id` varchar(64) NOT NULL,
  `tracked` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
