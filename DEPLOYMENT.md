# Free portfolio deployment

The application runs on a Render **Free** Docker web service. A separate Aiven
**Free** MySQL service stores the data. No paid Render database or disk is needed.
`render.yaml` explicitly selects `plan: free`.

Live demo: https://lily-clairevelle-fabrics.onrender.com

This is a portfolio demo: it has no payment processing or shipping integration.
Render's free service sleeps after inactivity, and a restart clears login sessions
and shopping bags. Orders and accounts remain in MySQL. Free-plan availability
and limits are controlled by the providers; do not accept an upgrade or enter a
paid plan to follow these instructions.

## 1. Create the free database

1. Sign in at https://console.aiven.io and create an **Aiven for MySQL Free** service.
2. Wait until the service is running. Keep the existing `defaultdb` database.
3. Find its host, port, username, and password under connection information.
4. Use those values only in Render's environment settings, never in Git or chat.

Do **not** run `db.sql` against this service. That file drops and recreates a
local database. The app creates its own tables and can seed the sample fabrics.

## 2. Deploy the application

Connect this GitHub repository to Render and create a Blueprint from the branch
containing these deployment files. The Blueprint defines one Free Docker service.
Alternatively create a Free web service, select Docker, and enter the variables
below manually. Use `/health` as the health-check path.
For a public repository connection, publish changes to GitHub, then select
**Manual Deploy > Deploy latest commit** in Render.

| Variable | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED` |
| `DB_USERNAME` | Aiven's database username |
| `DB_PASSWORD` | Aiven's database password |
| `ADMIN_EMAIL` | Your admin login email |
| `ADMIN_PASSWORD` | A unique password of 12–20 characters |
| `SEED_PRODUCTS` | `true` for the initial demo collection |

The database connection above requires TLS encryption. For certificate identity
verification, configure Connector/J's trusted CA store and `sslMode=VERIFY_IDENTITY`
as described in the linked MySQL documentation. Do not turn SSL off.

Render supplies `PORT`. The container runs as a non-root user, uses Java 21,
and limits heap size for the free service's memory allowance. Credentials are
excluded from the image build context.

## 3. Verify the live application

- Open the generated HTTPS `onrender.com` address and `/health`.
- Browse and view fabrics without logging in.
- Add a fabric as a guest, then create/log in to a customer account and verify
  the selection is added once after login.
- Place a demo order and refresh its confirmation; it must not create a duplicate.
- Sign in at `/login-admin` with your bootstrap admin credentials.
- Check the product and order pages. Public admin registration is disabled.
- A customer must not be able to view or cancel another customer's order.

The initial admin is created only if its email does not already exist. Subsequent
deploys do not reset its password. After first creation, remove `ADMIN_PASSWORD`
and `ADMIN_EMAIL` from Render if you no longer need bootstrap provisioning.
Changing those variables is not a password-reset mechanism.

`SEED_PRODUCTS=true` only inserts demo products when the product table is empty.
Set it to `false` after first deployment if an intentionally empty collection
should stay empty across restarts.

## Local development and tests

Use Java 21 and Maven. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in your
environment, or place them in the ignored `.env.local.properties` file:

```properties
DB_URL=jdbc:mysql://localhost:3306/webshop
DB_USERNAME=your_local_user
DB_PASSWORD=your_local_password
```

Run `mvn spring-boot:run`. Run `mvn test` for the test suite, which uses an isolated
H2 database and does not require or alter your local MySQL data.

New and updated passwords are BCrypt hashes. Existing local plaintext accounts
are migrated after a successful local login; the `prod` profile rejects plaintext
passwords. Use a fresh hosted database rather than copying the old account table.
Form submissions require a session CSRF token. API mutations additionally require
an authenticated admin session and an `X-CSRF-Token` header.

Mail delivery is not enabled by checkout. Optional SMTP configuration uses
`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`. An email app password
previously committed to this repository must be revoked at the email provider;
deleting it from the current files does not remove it from Git history.

The production profile runs `schema-hosted.sql` before Hibernate updates the
schema. This creates missing order-link tables with primary keys required by
Aiven, without removing existing records. Schema failures stop startup.

References: [Render free services](https://render.com/docs/free),
[Render Docker](https://render.com/docs/docker),
[Aiven free MySQL](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier),
[Connector/J SSL configuration](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-using-ssl.html).
