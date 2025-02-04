INSERT INTO roles (id, name) VALUES (1, 'ADMIN'), (2, 'USER');

INSERT INTO cars (id, model, brand, inventory, daily_fee, type)
VALUES (4, 'Astra','Opel', 1, 200.0, 'SEDAN');

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (4, 'email@mail.com', '1qw2', 'Electronic', 'Mail');

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
VALUES (1, '2025-02-20', '2025-02-28', '2025-02-27', 4, 4);

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
VALUES (2, '2025-02-20', '2025-02-28', '2025-02-27', 4, 4);

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
VALUES (3, '2025-02-20', '2025-02-28', '2025-02-27', 4, 4);

INSERT INTO users_roles (user_id, role_id)
VALUES (4, 2);

INSERT INTO payments (type, status, rental_id, total, session_url, session_id)
VALUES ('PAYMENT', 'PAID', 1, 22.0, '123', 'adfefww');

INSERT INTO payments (type, status, rental_id, total, session_url, session_id)
VALUES ('FINE', 'PENDING', 2, 22.0, '321', 'dsfef');