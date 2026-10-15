package se.iths.webshop.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class TemplateSettings {
    @Value("${app.admin-registration-enabled:false}")
    private boolean adminRegistrationEnabled;

    @ModelAttribute("csrfToken")
    public String csrfToken(HttpServletRequest request) {
        return WebSecurity.csrfToken(request);
    }

    @ModelAttribute("adminRegistrationEnabled")
    public boolean adminRegistrationEnabled() {
        return adminRegistrationEnabled;
    }
}
