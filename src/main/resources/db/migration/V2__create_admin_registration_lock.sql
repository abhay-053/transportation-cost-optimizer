CREATE TABLE admin_registration_lock (
                                         id INTEGER PRIMARY KEY,
                                         description VARCHAR(100) NOT NULL
);

INSERT INTO admin_registration_lock (id, description)
VALUES (1, 'Initial admin registration lock');