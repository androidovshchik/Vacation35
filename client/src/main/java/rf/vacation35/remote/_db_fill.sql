INSERT INTO
    _users(u_name, u_login, u_password, u_access_booking, u_access_price, u_admin)
VALUES
    ('Админ', 'admin', 'отпуск35рф', TRUE, TRUE, TRUE);

INSERT INTO
    _bases(ba_name)
VALUES
    ('ТестБ1'),
    ('ТестБ2');

INSERT INTO
    _buildings(bu_base, bu_name, bu_color)
VALUES
    (1, 'ПостройкаК', '#ff0000'),
    (1, 'ПостройкаЗ', '#00ff00'),
    (2, 'ПостройкаС', '#0000ff');

INSERT INTO
    _bookings(bo_building, bo_entry_time, bo_exit_time, bo_client_name, bo_phone, bo_bid)
VALUES
    (1, 1664618400, 1665086400, 'Пол1', '+77058025009', FALSE),
    (1, 1665136800, 1665518400, 'Пол2', '+79100942590', FALSE),
    (1, 1665741600, 1666209600, 'Пол1', '+77058025009', TRUE),
    (2, 1666346400, 1667160000, 'Пол2', '+79100942590', TRUE);
