package se.iths.webshop.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Comparator;
import java.util.Locale;
import se.iths.webshop.business.entity.Customer;
import se.iths.webshop.business.entity.CustomerOrder;
import se.iths.webshop.business.entity.Employee;
import se.iths.webshop.business.entity.Product;
import se.iths.webshop.business.service.MailService;
import se.iths.webshop.business.service.WebShopService;

@Controller
public class WebShopController {
    private static final String PENDING_CART_ADDITION = "pendingCartAddition";

    private record PendingCartAddition(long productId, int count, String query, String category, String sort)
            implements java.io.Serializable {}

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    WebShopService webShopService;

    @Autowired
    MailService mailService;

    public void setWebShopService(WebShopService webShopService) {
        this.webShopService = webShopService;
    }

    @GetMapping("/register-user")
    public String register(Model model) {
        model.addAttribute("customer", new Customer());
        return "reg";
    }

    @PostMapping("/register-user")
    public String registerUser(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br) {
        if (br.hasErrors()) {
            model.addAttribute("regcheck", "Name must be between 2 and 30 characters, password must be between 5 and 20 characters");
        } else {
            String check =  webShopService.registerUser(customer);
            model.addAttribute("regcheck", check);
        }
        return "reg";
    }

    @GetMapping("/register-admin")
    public String registerAdmin(Model model) {
        model.addAttribute("employee", new Employee());
        return "admin/admin-reg";
    }

    @PostMapping("/register-admin")
    public String registerAdmin(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br) {
        if (br.hasErrors()) {
            model.addAttribute("regcheck", "Name must be between 2 and 30 characters, password must be between 5 and 20 characters");
        } else {
            String check =  webShopService.registerAdmin(employee);
            model.addAttribute("regcheck", check);
        }
        return "admin/admin-reg";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        model.addAttribute("pendingAddition", session.getAttribute(PENDING_CART_ADDITION) instanceof PendingCartAddition);
        return "login";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        return "admin/admin";
    }

    @GetMapping("/login-admin")
    public String loginAdmin(Model model) {
        return "admin/admin-login";
    }

    @PostMapping("/login")
    public String loginUser(Model model, @RequestParam String email, @RequestParam String password,
            HttpSession session, RedirectAttributes redirect) {
        String login = webShopService.loginUser(email, password);
        model.addAttribute("login", login);
        if ("Wrong password or e-mail".equals(login) || !(webShopService.getUser() instanceof Customer)) {
            model.addAttribute("pendingAddition", session.getAttribute(PENDING_CART_ADDITION) instanceof PendingCartAddition);
            return "login";
        }
        if (session.getAttribute(PENDING_CART_ADDITION) instanceof PendingCartAddition pending) {
            String message = webShopService.addProductToCart(pending.productId(), pending.count());
            session.removeAttribute(PENDING_CART_ADDITION);
            redirect.addFlashAttribute("message", message);
            return browseLocation(pending.query(), pending.category(), pending.sort());
        }
        return "redirect:/home";
    }

    @PostMapping("/login-admin")
    public String loginAdmin(Model model, @RequestParam String email, @RequestParam String password) {
        String login = webShopService.loginAdmin(email, password);
        model.addAttribute("login", login);
        model.addAttribute("products", webShopService.getProducts());
        if (login == "Admin") {
            model.addAttribute("login", webShopService.getUser().getName());
            return "admin/admin-products";
        }
        return "admin/admin-login";
    }

    @GetMapping("/home")
    public String home(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("products", webShopService.getProducts());
            return "shop/home";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/browse")
    public String browse(Model model,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "name") String sort) {
        String query = q.trim();
        String selectedCategory = category.trim();
        String normalized = query.toLowerCase(Locale.ROOT);
        Comparator<Product> ordering = switch (sort) {
            case "price-asc" -> Comparator.comparing(Product::getPrice);
            case "price-desc" -> Comparator.comparing(Product::getPrice).reversed();
            default -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        };
        var products = webShopService.getProducts().stream()
                .filter(product -> selectedCategory.isEmpty() || product.getCategory().equalsIgnoreCase(selectedCategory))
                .filter(product -> normalized.isEmpty() || (product.getName() + " " + product.getCategory() + " "
                        + java.util.Objects.toString(product.getDescription(), "")).toLowerCase(Locale.ROOT).contains(normalized))
                .sorted(ordering).toList();
        addStorefrontAccount(model);
        model.addAttribute("categories", webShopService.getCategories());
        model.addAttribute("products", products);
        model.addAttribute("q", query);
        model.addAttribute("category", selectedCategory);
        model.addAttribute("sort", sort.equals("price-asc") || sort.equals("price-desc") ? sort : "name");
        return "shop/browse";
    }

    @PostMapping("/browse/add")
    public String addFromBrowse(@RequestParam long id, @RequestParam int count,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "name") String sort, RedirectAttributes redirect, HttpSession session) {
        if (count <= 0) {
            redirect.addFlashAttribute("message", "Quantity must be greater than zero!");
            return browseLocation(q, category, sort);
        }
        if (!(webShopService.getUser() instanceof Customer)) {
            session.setAttribute(PENDING_CART_ADDITION, new PendingCartAddition(id, count, q, category, sort));
            return "redirect:/login";
        }
        redirect.addFlashAttribute("message", webShopService.addProductToCart(id, count));
        return browseLocation(q, category, sort);
    }

    private void addStorefrontAccount(Model model) {
        boolean authenticated = webShopService.getUser() instanceof Customer;
        model.addAttribute("authenticated", authenticated);
        if (authenticated) {
            model.addAttribute("login", webShopService.getUser().getName());
        }
    }

    private String browseLocation(String q, String category, String sort) {
        return "redirect:" + UriComponentsBuilder.fromPath("/browse").queryParam("q", q)
                .queryParam("category", category).queryParam("sort", sort).build().encode().toUriString();
    }

    @PostMapping("/product")
    public String productView(Model model, @RequestParam long id) {
        addStorefrontAccount(model);
        model.addAttribute("categories", webShopService.getCategories());
        model.addAttribute("product", webShopService.getProduct(id));
        return "shop/product-view";
    }
    @PostMapping("/product-add")
    public String productAdd(Model model, @RequestParam long id, @RequestParam int count,
            HttpSession session, RedirectAttributes redirect) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("product", webShopService.getProduct(id));
            model.addAttribute("message", webShopService.addProductToCart(id ,count));
            addStorefrontAccount(model);
            return "shop/product-view";
        }
        return addFromBrowse(id, count, "", "", "name", redirect, session);
    }


    @PostMapping("/update-cart")
    public String updateCartItem(Model model, @RequestParam String name, @RequestParam int count) {
        if (webShopService.getUser() instanceof Customer) {
            webShopService.updateCartItem(name, count);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("items", webShopService.getCartItems());
            model.addAttribute("total", "Total: " + webShopService.getCartTotal() + " SEK");
            model.addAttribute("emptycart", webShopService.getCart().getItems().size() < 1);
            return "shop/cart-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/clear-cart")
    public String deleteCart(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("items", webShopService.getCartItems());
            model.addAttribute("changes", webShopService.clearCart());
            model.addAttribute("total", "Total: " + webShopService.getCartTotal() + " SEK");
            model.addAttribute("emptycart", webShopService.getCart().getItems().size() < 1);
            return "shop/cart-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/place-order")
    public String placeOrder(Model model, HttpSession session) {
        if (webShopService.getUser() instanceof Customer) {
            if (webShopService.getCartItems().isEmpty()) {
                return "redirect:/cart";
            }
            CustomerOrder order = webShopService.checkout();
            webShopService.clearCart();
            session.setAttribute("placedOrderId", order.getId());
            return "redirect:/order-confirmation";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/order-confirmation")
    public String orderConfirmation(Model model, HttpSession session) {
        if (!(webShopService.getUser() instanceof Customer customer)) {
            return "redirect:/login";
        }
        Object placedOrderId = session.getAttribute("placedOrderId");
        if (!(placedOrderId instanceof Long id)) {
            return "redirect:/orders";
        }
        CustomerOrder order = webShopService.getOrder(id);
        if (order == null || order.getCustomer() == null || customer.getId() == null
                || !customer.getId().equals(order.getCustomer().getId())) {
            return "redirect:/orders";
        }
        model.addAttribute("login", customer.getName());
        model.addAttribute("order", order);
        return "shop/confirmed";
    }

    @PostMapping("/checkout")
    public String checkout(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("items", webShopService.getCartItems());
            model.addAttribute("total", "Total: " + webShopService.getCartTotal() + " SEK");
            model.addAttribute("categories", webShopService.getCategories());
            return "shop/checkout";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/cart")
    public String cartView(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("items", webShopService.getCartItems());
            model.addAttribute("total", "Total: " + webShopService.getCartTotal() + " SEK");
            model.addAttribute("emptycart", webShopService.getCart().getItems().size() < 1);
            return "shop/cart-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("orders", webShopService.getUserOrders());
            return "shop/orders-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/order")
    public String orderView(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Customer) {
            CustomerOrder order = webShopService.getOrder(id);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("id", id);
            model.addAttribute("name", order.getName());
            model.addAttribute("items", order.getItems());
            model.addAttribute("total", order.getTotalPrice());
            model.addAttribute("confirm", order.isConfirmed());
            model.addAttribute("process", order.isProcessed());
            model.addAttribute("cancel", order.isCanceled());
            return "shop/order-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/order-cancel")
    public String orderCancel(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Customer) {
            CustomerOrder order = webShopService.getOrder(id);
            webShopService.cancelOrder(id);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("id", id);
            model.addAttribute("name", order.getName());
            model.addAttribute("items", order.getItems());
            model.addAttribute("total", order.getTotalPrice());
            model.addAttribute("confirm", order.isConfirmed());
            model.addAttribute("process", order.isProcessed());
            model.addAttribute("cancel", order.isCanceled());
            return "shop/order-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/profile")
    public String profileView(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            return "shop/profile";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/edit-name")
    public String editName(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit", "");
            return "shop/edit-name";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/edit-name")
    public String updateName(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br, @RequestParam(value = "oldPassword", required = true) String password) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", customer.getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            if (br.hasErrors()) {
                model.addAttribute("edit", "Name must be between 2 and 30 characters");
                return "shop/edit-name";
            } else {
                String check =  webShopService.updateName(customer, password);
                model.addAttribute("edit", check);
                return "shop/edit-name";
            }
        } else {
            model.addAttribute("login", "Please log in first");
        return "login";
        }
    }

    @GetMapping("/edit-email")
    public String editEmail(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit", "");
            return "shop/edit-email";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/edit-email")
    public String updateEmail(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br, @RequestParam(value = "oldPassword", required = true) String password) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", customer.getEmail());
            if (br.hasErrors()) {
                model.addAttribute("edit", "Must be a valid e-mail address");
            } else {
                String check =  webShopService.updateEmail(customer, password);
                model.addAttribute("edit", check);
                return "shop/edit-email";
            }
        } else {
            model.addAttribute("login", "Please log in first");
        }
        return "login";
    }

        @GetMapping("/edit-password")
    public String editPassword(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit", "");
            return "shop/edit-password";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/edit-password")
    public String updatePassword(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br, @RequestParam(value = "repeatedPassword", required = true) String repeatedPassword , @RequestParam(value = "oldPassword", required = true) String password) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            if (br.hasErrors()) {
                model.addAttribute("edit", "Password must be between 5 and 20 characters");
                return "shop/edit-password";
            } else {
                if (customer.getPassword().equals(repeatedPassword)) {
                    String check =  webShopService.updatePassword(customer, password);
                    model.addAttribute("edit", check);
                } else {
                    model.addAttribute("edit", "Please repeat the new password");
                }
                return "shop/edit-password";
            }
        } else {
            model.addAttribute("login", "Please log in first");
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logoutUser(Model model) {
        String logout = webShopService.logoutUser();
        model.addAttribute("login", logout);
        return "login";
    }

    @GetMapping("/logout-admin")
    public String logoutAdmin(Model model) {
        String logout = webShopService.logoutUser();
        model.addAttribute("login", logout);
        return "admin/admin";
    }

        @GetMapping("/admin-profile")
    public String adminProfileView(Model model) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            return "admin/admin-profile";
        }
        model.addAttribute("login", "Please log in first");
        return "admin/admin-login";
    }

    @GetMapping("/admin-edit-name")
    public String adminEditName(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit", "");
            return "admin/admin-edit-name";
        }
        model.addAttribute("login", "Please log in first");
        return "admin/admin-login";
    }

    @PostMapping("/admin-edit-name")
    public String adminUpdateName(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br, @RequestParam(value = "oldPassword", required = true) String password) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", employee.getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            if (br.hasErrors()) {
                model.addAttribute("edit", "Name must be between 2 and 30 characters");
                return "admin/admin-edit-name";
            } else {
                String check =  webShopService.adminUpdateName(employee, password);
                model.addAttribute("edit", check);
                return "admin/admin-edit-name";
            }
        } else {
            model.addAttribute("login", "Please log in first");
        return "admin/admin-login";
        }
    }

    @GetMapping("/admin-edit-email")
    public String adminEditEmail(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit", "");
            return "admin/admin-edit-email";
        }
        model.addAttribute("login", "Please log in first");
        return "admin/admin-login";
    }

    @PostMapping("/admin-edit-email")
    public String adminUpdateEmail(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br, @RequestParam(value = "oldPassword", required = true) String password) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", employee.getEmail());
            if (br.hasErrors()) {
                model.addAttribute("edit", "Must be a valid e-mail address");
            } else {
                String check =  webShopService.adminUpdateEmail(employee, password);
                model.addAttribute("edit", check);
                return "admin/admin-edit-email";
            }
        } else {
            model.addAttribute("login", "Please log in first");
        }
        return "admin/admin-login";
    }

        @GetMapping("/admin-edit-password")
    public String adminEditPassword(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit", "");
            return "admin/admin-edit-password";
        }
        model.addAttribute("login", "Please log in first");
        return "admin-login";
    }

    @PostMapping("/admin-edit-password")
    public String adminUpdatePassword(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br, @RequestParam(value = "repeatedPassword", required = true) String repeatedPassword , @RequestParam(value = "oldPassword", required = true) String password) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            if (br.hasErrors()) {
                model.addAttribute("edit", "Password must be between 5 and 20 characters");
                return "admin/admin-edit-password";
            } else {
                if (employee.getPassword().equals(repeatedPassword)) {
                    String check =  webShopService.adminUpdatePassword(employee, password);
                    model.addAttribute("edit", check);
                } else {
                    model.addAttribute("edit", "Please repeat the new password");
                }
                return "admin/admin-edit-password";
            }
        } else {
            model.addAttribute("login", "Please log in first");
        }
        return "admin/admin-login";
    }

    @GetMapping("admin-add")
    public String adminViewAdd(Model model) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("product", new Product());
            return "admin/admin-add";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-add")
    public String addProduct(Model model, @Valid @ModelAttribute("product") Product product, BindingResult br) {
        if (webShopService.getUser() instanceof Employee) {
            if (br.hasErrors()) {
                model.addAttribute("addResult", "Invalid input");
            } else {
                String add = webShopService.addProduct(product);
                model.addAttribute("addResult", add);
                model.addAttribute("login", webShopService.getUser().getName());
            }
            return "admin/admin-add";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/remove")
    public String removeProduct(Model model, @RequestParam long id, @RequestParam String text) {
        if (webShopService.getUser() instanceof Employee) {
            String remove = webShopService.removeProduct(id);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("products", webShopService.searchProductsByName(text));
            model.addAttribute("remove", remove);
            return "admin/admin-products";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/admin-view")
    public String adminViewAll(Model model) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("products", webShopService.getProducts());
            model.addAttribute("login", webShopService.getUser().getName());
            return "admin/admin-products";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-search")
    public String adminViewSearch(Model model, @RequestParam String text) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("products", webShopService.searchProductsByName(text));
            return "admin/admin-products";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/admin-pending")
    public String adminViewPending(Model model) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("orders", webShopService.getPendingOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            return "admin/admin-pending";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }
    @PostMapping("/admin-pending-confirm")
    public String adminViewPendingConfirm(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            webShopService.confirmOrder(id);
            model.addAttribute("orders", webShopService.getPendingOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("message", "Order confirmed!");
            return "admin/admin-pending";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-pending-order")
    public String adminViewPendingOrder(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            CustomerOrder order = webShopService.getOrder(id);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("order", order.getName());
            model.addAttribute("items", order.getItems());
            model.addAttribute("total", order.getTotalPrice());
            model.addAttribute("confirm", order.isConfirmed());
            model.addAttribute("process", order.isProcessed());
            model.addAttribute("id", id);
            return "admin/admin-pending-order";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-pending-cancel")
    public String adminViewPendingCancel(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            webShopService.cancelOrder(id);
            model.addAttribute("orders", webShopService.getPendingOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("message", "Order canceled!");
            return "admin/admin-pending";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/admin-process")
    public String adminViewConfirmed(Model model) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("orders", webShopService.getConfirmedOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            return "admin/admin-process";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-process-confirm")
    public String adminViewProcessConfirm(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            webShopService.processOrder(id);
            model.addAttribute("orders", webShopService.getPendingOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("message", "Order processed!");
            return "admin/admin-process";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-process-order")
    public String adminViewConfirmedOrder(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            CustomerOrder order = webShopService.getOrder(id);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("order", order.getName());
            model.addAttribute("items", order.getItems());
            model.addAttribute("total", order.getTotalPrice());
            model.addAttribute("confirm", order.isConfirmed());
            model.addAttribute("process", order.isProcessed());
            model.addAttribute("id", id);
            return "admin/admin-process-order";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-process-cancel")
    public String adminViewConfirmedCancel(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            webShopService.cancelOrder(id);
            model.addAttribute("orders", webShopService.getPendingOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("message", "Order has been canceled!");
            return "admin/admin-process";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/admin-dispatch")
    public String adminViewDispatched(Model model) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("orders", webShopService.getDispatchedOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            return "admin/admin-dispatch";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-dispatch-order")
    public String adminViewDispatchedOrder(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            CustomerOrder order = webShopService.getOrder(id);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("order", order.getName());
            model.addAttribute("items", order.getItems());
            model.addAttribute("total", order.getTotalPrice());
            model.addAttribute("confirm", order.isConfirmed());
            model.addAttribute("process", order.isProcessed());
            return "admin/admin-dispatch-order";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @GetMapping("/admin-cancel")
    public String adminViewCanceled(Model model) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("orders", webShopService.getCanceledOrders());
            model.addAttribute("login", webShopService.getUser().getName());
            return "admin/admin-cancel";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/admin-cancel-order")
    public String adminViewCanceledOrder(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Employee) {
            CustomerOrder order = webShopService.getOrder(id);
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("order", order.getName());
            model.addAttribute("items", order.getItems());
            model.addAttribute("total", order.getTotalPrice());
            model.addAttribute("confirm", order.isConfirmed());
            model.addAttribute("process", order.isProcessed());
            return "admin/admin-cancel-order";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }
}
