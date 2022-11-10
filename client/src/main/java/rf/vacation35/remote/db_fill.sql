INSERT INTO
    bases(ba_name)
VALUES
    ('ТестБ1'),
    ('ТестБ2');

INSERT INTO
    buildings(bu_base, bu_name, bu_color)
VALUES
    (1, 'СтроениеК', '#ff0000'),
    (1, 'СтроениеЗ', '#00ff00'),
    (2, 'СтроениеС', '#0000ff');

INSERT INTO
    prices(p_building, p_start_sec, p_end_sec, p_rub, p_discount_per)
VALUES
    (1, 43200, 432000, 1000, 20),
    (1, 432000, 475200, 1200, 0),
    (1, 388800, 604800, 1200, 0),
    (2, 0, 604800, 1500, 0);

INSERT INTO
    bookings(bo_building, bo_entry_time, bo_exit_time, bo_client_name, bo_phone, bo_alert, bo_bid)
VALUES
    (1, '2022-10-01 10:00:00', '2022-10-06 20:00:00', 'Пол1', '+77058025009', TRUE, FALSE),
    (1, '2022-10-07 10:00:00', '2022-10-11 20:00:00', 'Пол2', '+77058025009', TRUE, FALSE),
    (1, '2022-10-14 10:00:00', '2022-10-19 20:00:00', 'Пол1', '+77058025009', TRUE, TRUE),
    (1, '2022-10-21 10:00:00', '2022-10-30 20:00:00', 'Пол2', '+77058025009', TRUE, TRUE);
