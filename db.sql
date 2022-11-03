CREATE TABLE users(
    u_id MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT,
    u_name VARCHAR(60) NOT NULL,
    u_login VARCHAR(30) NOT NULL,
    u_password VARCHAR(15) NOT NULL,
    u_is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (u_id)
) ENGINE=InnoDB CHARACTER SET=UTF8;

INSERT INTO
    users(u_name, u_login, u_password, u_is_admin)
VALUES
    ('Админ', 'admin', 'отпуск35рф', TRUE);
