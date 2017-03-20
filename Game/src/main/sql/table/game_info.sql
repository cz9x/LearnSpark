CREATE TABLE `game_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `home_team` varchar(50) DEFAULT NULL,
  `guest_team` varchar(50) DEFAULT NULL,
  `game_time` datetime DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `update_ind` (`home_team`,`guest_team`,`game_time`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
