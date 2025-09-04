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

INSERT INTO product (category, description, name, price) VALUES ('Phone', 'The latest iPhone model', 'iPhone 14', 12000.0);
INSERT INTO product (category, description, name, price) VALUES ('Laptop', 'The fastest MacBook Pro', 'MacBook Pro', 24000.0);
INSERT INTO product (category, description, name, price) VALUES ('TV', 'New OLED TV from Sony', 'SONY 50" 4K TV', 17000.0);
INSERT INTO product (category, description, name, price) VALUES ('Phone', 'The latest Samsung phone', 'Samsung Galaxy 22', 11000.0);

