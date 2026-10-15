package se.iths.webshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import se.iths.webshop.business.entity.Customer;
import se.iths.webshop.business.entity.Employee;
import se.iths.webshop.business.service.WebShopService;

/** Applies to the app's existing session-based login without replacing its UI. */
@Configuration
public class WebSecurity implements WebMvcConfigurer, HandlerInterceptor {
    private final WebShopService service;
    private final boolean registrationEnabled;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> CUSTOMER_PAGES = Set.of("/cart", "/checkout", "/place-order", "/orders",
            "/order", "/order-cancel", "/order-confirmation", "/profile", "/edit-name", "/edit-email",
            "/edit-password", "/update-cart", "/clear-cart");

    public WebSecurity(WebShopService service, @Value("${app.admin-registration-enabled:false}") boolean enabled) {
        this.service = service;
        this.registrationEnabled = enabled;
    }

    public static String csrfToken(HttpServletRequest request) {
        var session = request.getSession();
        if (session.getAttribute("csrfToken") == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            session.setAttribute("csrfToken", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }
        return (String) session.getAttribute("csrfToken");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("Referrer-Policy", "same-origin");
                response.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' data: https:; "
                        + "style-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none'; "
                        + "base-uri 'self'; form-action 'self'");
                if (!(handler instanceof HandlerMethod) || request.getRequestURI().equals("/error")) return true;
                String path = request.getRequestURI();
                boolean safe = Set.of("GET", "HEAD", "OPTIONS").contains(request.getMethod());
                if (path.equals("/register-admin") && !registrationEnabled) {
                    response.sendError(404);
                    return false;
                }
                boolean adminOnly = path.startsWith("/admin-") || path.equals("/remove")
                        || (path.startsWith("/rs/") && !safe);
                if (adminOnly && !(service.getUser() instanceof Employee)) {
                    response.sendError(403);
                    return false;
                }
                if (CUSTOMER_PAGES.contains(path) && !(service.getUser() instanceof Customer)) {
                    response.sendRedirect("/login");
                    return false;
                }
                String expected = csrfToken(request);
                if (!safe) {
                    String supplied = request.getParameter("_csrf");
                    if (supplied == null) supplied = request.getHeader("X-CSRF-Token");
                    if (supplied == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                            supplied.getBytes(StandardCharsets.UTF_8))) {
                        response.sendError(403);
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response,
                    Object handler, ModelAndView model) {
                if (request.getMethod().equals("POST") && Set.of("/login", "/login-admin").contains(request.getRequestURI())
                        && service.getUser() != null && request.getSession(false) != null) {
                    request.changeSessionId();
                }
            }
}
