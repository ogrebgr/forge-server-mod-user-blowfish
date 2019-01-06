CREATE TABLE `user_blowfish` (
  `user` int(11) NOT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `username_lc` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`user`),
  KEY `i_username` (`username`),
  KEY `i_username_lc` (`username_lc`),
  CONSTRAINT `fk_user_blowfish_1` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `user_blowfish_post_auto` (  `user` int(11) NOT NULL,  `username` varchar(255) NOT NULL,  `password` varchar(255) NOT NULL, `username_lc` varchar(255) NOT NULL,  PRIMARY KEY (`user`),  KEY `i_username` (`username`),  KEY `i_username_lc` (`username_lc`),  CONSTRAINT `fk_user_blowfish_2` FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB DEFAULT CHARSET=utf8;

