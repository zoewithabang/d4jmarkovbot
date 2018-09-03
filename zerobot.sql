CREATE TABLE IF NOT EXISTS `users` (
  `id` varchar(64) NOT NULL,
  `tracked` tinyint(1) NOT NULL,
  `permission_rank` int(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `aliases` (
  `alias` varchar(191) NOT NULL,
  `command` varchar(191) NOT NULL,
  `description` varchar(191) NOT NULL,
  PRIMARY KEY (`alias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `bot_messages` (
  `name` varchar(191) NOT NULL,
  `message` varchar(1024) NOT NULL,
  `description` varchar(191) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `commands`;
CREATE TABLE `commands` (
  `command` varchar(191) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `permission_rank` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY (`command`)
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
  `key` varchar(191) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tasks` (
  `task` varchar(191) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `initial_delay` int NOT NULL,
  `period` int NOT NULL,
  PRIMARY KEY (`task`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('alias', 1, 10);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('aliases', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('cat', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('command', 1, 255);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('commands', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('dog', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('getposts', 1, 20);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('help', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('markov', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('messages', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('music', 0, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('np', 0, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('rank', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('say', 1, 0);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('setsay', 1, 5);
INSERT INTO commands (`command`, `active`, `permission_rank`) VALUES ('user', 1, 5);

INSERT INTO options (`key`, `value`) VALUES ('cat_api_key', '');
INSERT INTO options (`key`, `value`) VALUES ('colour', '#FACFFF');
INSERT INTO options (`key`, `value`) VALUES ('cytube_log_location', '/home/example/cytube/chanlogs/channel.log');
INSERT INTO options (`key`, `value`) VALUES ('cytube_url', 'https://www.example.com/r/channel');
INSERT INTO options (`key`, `value`) VALUES ('dog_api_key', '');
INSERT INTO options (`key`, `value`) VALUES ('markov_message_count', '200000');
INSERT INTO options (`key`, `value`) VALUES ('markov_output_length', '20');
INSERT INTO options (`key`, `value`) VALUES ('name', 'ZeroBot');

INSERT INTO tasks (`task`, `active`, `initial_delay`, `period`) VALUES ('cytubeNp', 0, 2, 2);

INSERT INTO users (`id`, `tracked`, `permission_rank`) VALUES ('insertyouridhere', 1, 255);