INSERT INTO roles (id, name) VALUES (1, 'ADMIN'), (2, 'USER');

INSERT INTO cars (id, model, brand, inventory, daily_fee, type)
VALUES (4, 'Astra','Opel', 1, 200.00, 'SEDAN');

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (4, 'email@mail.com', '1qw2', 'Electronic', 'Mail');

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
VALUES (1, CURRENT_DATE, CURRENT_DATE + INTERVAL '20' DAY, CURRENT_DATE + INTERVAL '19' DAY, 4, 4);

INSERT INTO rentals (id, rental_date, return_date, car_id, user_id)
VALUES (2, CURRENT_DATE, CURRENT_DATE + INTERVAL '6' DAY, 4, 4);


INSERT INTO users_roles (user_id, role_id)
VALUES (4, 2);
