DROP DATABASE IF EXISTS webshop;

CREATE DATABASE webshop;

USE webshop;

CREATE TABLE product (
	id BIGINT NOT NULL AUTO_INCREMENT,
	category VARCHAR(255),
	description VARCHAR(255),
	name VARCHAR(255),
	price DOUBLE NOT NULL,
	img VARCHAR(255),
	PRIMARY KEY (id)
);

INSERT INTO product (category, description, name, price, img) VALUES ('Linen', 'White linen fabric', 'White linen', 300.0, '/images/linen_texture.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Knit', 'Soft grey knitted fabric', 'Grey knit', 250.0, '/images/knit_texture.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Wool', 'Warm beige wool fabric', 'Beige wool', 200.0, '/images/wool_texture.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Silk', 'Smooth black silk fabric', 'Black silk', 400.0, '/images/black_fabric.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Fur', 'Leopard fur fabric', 'Animal fur', 300.0, '/images/animal_print.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Silk', 'Soft gold silk fabric', 'Gold silk', 250.0, '/images/silky_brown_fabric.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Cloth', 'Terry cloth fabric', 'Terry cloth', 200.0, '/images/gray_texture.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Cloth', 'Vivid pink cloth fabric', 'Pink cloth', 400.0, '/images/pink_fabric.jpg');
INSERT INTO product (category, description, name, price, img) VALUES ('Cloth', 'Dark blue cloth fabric', 'Blue cloth', 250.0, '/images/dark_blue_fabric.jpg');

