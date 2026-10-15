package se.iths.webshop.business.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Password hashes are never suitable for rendering back into a form. */
public final class Passwords {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    private Passwords() {}

    public static String hash(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean isHash(String value) {
        return value != null && value.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}");
    }

    public static boolean matches(String raw, String encoded) {
        return raw != null && isHash(encoded) && ENCODER.matches(raw, encoded);
    }

    public static boolean validInput(String raw) {
        return raw != null && raw.length() >= 5 && raw.length() <= 20;
    }
}
