INSERT INTO
    bases(ba_name)
VALUES
    ('ТестБ1'),
    ('ТестБ2');

INSERT INTO
    buildings(bu_base, bu_name, bu_color)
VALUES
    (1, 'ПостройкаК', '#ff0000'),
    (1, 'ПостройкаЗ', '#00ff00'),
    (2, 'ПостройкаС', '#0000ff');

INSERT INTO
    bookings(bo_building, bo_entry_time, bo_exit_time, bo_client_name, bo_phone, bo_bid)
VALUES
    (1, '2022-10-01 10:00:00', '2022-10-06 20:00:00', 'Пол1', '+77058025009', FALSE),
    (1, '2022-10-07 10:00:00', '2022-10-11 20:00:00', 'Пол2', '+77058025009', FALSE),
    (1, '2022-10-14 10:00:00', '2022-10-19 20:00:00', 'Пол1', '+77058025009', TRUE),
    (1, '2022-10-21 10:00:00', '2022-10-30 20:00:00', 'Пол2', '+77058025009', TRUE);
