INSERT INTO roles (id, name) VALUES (1, 'ADMIN'), (2, 'USER');

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (1, 'Admin@mail.com','1234', 'Admin', 'User');

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (2, 'User@mail.com', '4321', 'Regular', 'User');

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (3, 'Test@mail.com', '2314', 'Tset', 'User');

INSERT INTO users_roles (user_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 2),
    (1, 2);