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

INSERT INTO product (category, description, name, price) VALUES ('Linen', 'White linen fabric', 'White linen', 300.0);
INSERT INTO product (category, description, name, price) VALUES ('Knit', 'Soft grey knitted fabric', 'Grey knit', 250.0);
INSERT INTO product (category, description, name, price) VALUES ('Wool', 'Warm beige wool fabric', 'Beige wool', 200.0);
INSERT INTO product (category, description, name, price) VALUES ('Silk', 'Smooth black silk fabric', 'Black silk', 400.0);
INSERT INTO product (category, description, name, price) VALUES ('Fur', 'Leopard fur fabric', 'Animal fur', 300.0);
INSERT INTO product (category, description, name, price) VALUES ('Silk', 'Soft gold silk fabric', 'Gold silk', 250.0);
INSERT INTO product (category, description, name, price) VALUES ('Cloth', 'terry cloth fabric', 'Terry cloth', 200.0);
INSERT INTO product (category, description, name, price) VALUES ('Cloth', 'Vivid pink cloth fabric', 'Pink cloth', 400.0);

