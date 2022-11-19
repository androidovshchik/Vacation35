DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS buildings;
DROP TABLE IF EXISTS bases;
DROP TABLE IF EXISTS users;

CREATE TABLE users(
    u_id MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT,
    u_name VARCHAR(60) NOT NULL,
    u_login VARCHAR(30) NOT NULL UNIQUE,
    u_password VARCHAR(30) NOT NULL,
    u_access_price BOOLEAN NOT NULL DEFAULT FALSE,
    u_access_booking BOOLEAN NOT NULL DEFAULT FALSE,
    u_admin BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (u_id)
) ENGINE=InnoDB CHARACTER SET=UTF8;

INSERT INTO
    users(u_name, u_login, u_password, u_access_booking, u_access_price, u_admin)
VALUES
    ('Админ', 'admin', 'отпуск35рф', TRUE, TRUE, TRUE);

CREATE TABLE bases(
    ba_id MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ba_name VARCHAR(60) NOT NULL,
    PRIMARY KEY (ba_id)
) ENGINE=InnoDB CHARACTER SET=UTF8;

CREATE TABLE buildings(
    bu_id MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT,
    bu_base MEDIUMINT UNSIGNED NOT NULL,
    bu_name VARCHAR(60) NOT NULL,
    bu_color VARCHAR(20) NOT NULL,
    bu_entry_time TIME,
    bu_exit_time TIME,
    PRIMARY KEY (bu_id),
    FOREIGN KEY (bu_base) REFERENCES bases(ba_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET=UTF8;

CREATE TABLE bookings(
    bo_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    bo_building MEDIUMINT UNSIGNED,
    bo_entry_time DATETIME NOT NULL,
    bo_exit_time DATETIME NOT NULL,
    bo_client_name VARCHAR(200) NOT NULL,
    bo_phone VARCHAR(40) NOT NULL,
    bo_bid BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (bo_id),
    FOREIGN KEY (bo_building) REFERENCES buildings(bu_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB CHARACTER SET=UTF8;
