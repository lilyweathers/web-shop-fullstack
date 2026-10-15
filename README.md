# Fabric Store

A full-stack fabric shop for the love of making. Browse colours and textures, build a shopping bag, and follow an order from checkout to dispatch.

**[Visit the live shop ↗](https://lily-clairevelle-fabrics.onrender.com/)**

Built by **Lily Clairevelle**, originally as an assignment for the Complex Java Programming course, and developed further into a deployed portfolio project.

## Explore the shop

- **Browse freely:** search fabrics, filter by material, and sort by name or price without an account.
- **Find the details:** view fabric descriptions, images, and prices per square metre.
- **Keep your selection:** adding a fabric as a guest takes you to login, then returns you to browsing with the selected fabric in your bag.
- **Order and follow along:** update quantities, review checkout, see an order confirmation, and view or cancel eligible orders.
- **Manage your account:** edit your name, email address, and password.
- **Run the shop:** administrators can add and remove products and confirm, dispatch, or cancel orders through a dedicated dashboard.

The live site is a portfolio demo. Checkout creates demo orders; it does not collect payments or arrange shipping. Email delivery is not enabled.

## Built with

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot, Spring MVC |
| Frontend | Thymeleaf, HTML, CSS |
| Persistence | Spring Data JPA, Hibernate, MySQL |
| Authentication | Session-based login, BCrypt password hashing, CSRF protection |
| Tests | JUnit, Mockito, Spring MockMvc, H2 |
| Deployment | Docker on Render, MySQL on Aiven |

The application follows an MVC structure, with controllers handling web requests, services implementing shop behaviour, and repositories managing persistence.

## Run locally

### Requirements

- Java 21
- Maven
- A running MySQL server, installed locally or running in Docker

### Setup

1. Clone the repository and enter the project directory:

   ```sh
   git clone https://github.com/lilyweathers/web-shop-fullstack.git
   cd web-shop-fullstack
   ```

2. Create an empty MySQL database named `webshop` and give your local database user access to it.

3. Create `.env.local.properties` in the project root with your local connection details:

   ```properties
   DB_URL=jdbc:mysql://localhost:3306/webshop
   DB_USERNAME=your_local_user
   DB_PASSWORD=your_local_database_password
   SEED_PRODUCTS=true
   ```

   This file is ignored by Git. The app creates its tables automatically; `SEED_PRODUCTS=true` adds the sample fabric collection when the product table is empty. You can also supply these settings as environment variables.

4. Start the application:

   ```sh
   mvn spring-boot:run
   ```

5. Open [localhost:8080](http://localhost:8080). Browse immediately, or create a customer account to use the shopping bag and checkout.

Use a fresh database for this setup. The legacy `db.sql` script drops and recreates a database and is not needed for the steps above.

### Admin access

Public admin registration is disabled by default. To create an initial admin, add `ADMIN_EMAIL` and `ADMIN_PASSWORD` to your local configuration or environment before starting the app. The admin password must contain 12–20 characters. Use unique credentials and keep them out of Git.

Sign in at [/login-admin](http://localhost:8080/login-admin). Bootstrap settings create an admin only when the email does not already exist; they do not reset an existing account's password. Remove both settings after the account has been created.

## Tests

```sh
mvn test
```

Tests use an isolated H2 database, so they do not require or modify your local MySQL data. Coverage includes cart calculations, login and access controls, CSRF validation, order ownership, and the guest-to-checkout flow, including confirmation refreshes without duplicate orders.

## Hosting

The [live demo](https://lily-clairevelle-fabrics.onrender.com/) runs on Render's free web-service plan with an Aiven MySQL database. The service sleeps after inactivity, so the first visit may take a little longer to load. Login sessions and shopping bags are held in memory and reset when the app restarts; accounts and orders remain in MySQL.

See **[DEPLOYMENT.md](DEPLOYMENT.md)** for environment variables, Docker deployment, initial admin setup, and maintenance notes.

---

© 2026 Lily Clairevelle
