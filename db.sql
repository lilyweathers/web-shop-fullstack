DROP DATABASE IF EXISTS webshop;

CREATE DATABASE webshop;

USE webshop;

CREATE TABLE product (
	id BIGINT NOT NULL AUTO_INCREMENT,
	category VARCHAR(255),
	description VARCHAR(255),
	name VARCHAR(255),
	price DOUBLE NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO product (category, description, name, price) VALUES ('Linen', 'White linen fabric', 'White fabric', 300.0);
INSERT INTO product (category, description, name, price) VALUES ('Knit', 'Soft grey knitted fabric', 'Grey fabric', 250.0);
INSERT INTO product (category, description, name, price) VALUES ('Wool', 'Warm beige wool fabric', 'Beige fabric', 200.0);
INSERT INTO product (category, description, name, price) VALUES ('Silk', 'Smooth black silk fabric', 'Black fabric', 400.0);

