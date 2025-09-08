package se.iths.webshop.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import se.iths.webshop.business.entity.Customer;
import se.iths.webshop.business.entity.CustomerOrder;
import se.iths.webshop.business.entity.Employee;
import se.iths.webshop.business.entity.Product;
import se.iths.webshop.business.service.MailService;
import se.iths.webshop.business.service.WebShopService;

@Controller
public class WebShopController {

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
    public String login(Model model) {
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
    public String loginUser(Model model, @RequestParam String email, @RequestParam String password) {
        String login = webShopService.loginUser(email, password);
        model.addAttribute("login", login);
        model.addAttribute("products", webShopService.getProducts());
        if (login == "Wrong password or email!") {
            return "login";
        }
        model.addAttribute("categories", webShopService.getCategories());
        return "shop/home";
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
        return "login";
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

    @GetMapping("/search")
    public String searchView(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("products", webShopService.getProducts());
            return "shop/search";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/search")
    public String search(Model model, @RequestParam String text, @RequestParam(value = "searchBy", required = true) String searchBy) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            if ("name".equals(searchBy)) {
                model.addAttribute("products", webShopService.searchProductsByName(text));
            } else if ("category".equals(searchBy)){
                model.addAttribute("products", webShopService.searchProductsByCategory(text));
            }
            model.addAttribute("text", text);
            return "shop/search";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/product")
    public String productView(Model model, @RequestParam long id) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("product", webShopService.getProduct(id));
            return "shop/product-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }
    @PostMapping("/product-add")
    public String productAdd(Model model, @RequestParam long id, @RequestParam int count) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("product", webShopService.getProduct(id));
            model.addAttribute("message", webShopService.addProductToCart(id ,count));
            return "shop/product-view";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }


    @GetMapping("/category")
    public String category(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("products", webShopService.getProducts());
            return "shop/category";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @RequestMapping("/category/{category}")
    public String category(Model model, @PathVariable("category") String category) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("products", webShopService.searchProductsByCategory(category));
            return "shop/category";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/add-item")
    public String addToCart(Model model, @RequestParam long id, @RequestParam int count, @RequestParam String searched) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("products", webShopService.searchProductsByName(searched));
            model.addAttribute("message", webShopService.addProductToCart(id ,count));
            return "shop/search";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/add-item-category")
    public String addToCartFromCategory(Model model, @RequestParam long id, @RequestParam int count, @RequestParam String selected) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("products", webShopService.searchProductsByCategory(selected));
            model.addAttribute("message", webShopService.addProductToCart(id ,count));
            return "shop/category";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
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
    public String placeOrder(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            CustomerOrder order = webShopService.checkout();
            String text = System.getProperty("line.separator") + "Your order has been placed!" + System.getProperty("line.separator") + System.getProperty("line.separator")
                    + "Order number: " + order.getId() + System.getProperty("line.separator")
                    + order.getOrderItemsAsString() + System.getProperty("line.separator")
                    + System.getProperty("line.separator") + order.getTotalPrice();
            webShopService.clearCart();
            //mailService.sendEmail("Order no: " + order.getId(), text, webShopService.getUser().getEmail());
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("items", order.getItems());
            model.addAttribute("total", order.getTotalPrice());
            model.addAttribute("message", "Order has been placed!");
            return "shop/confirmed";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/checkout")
    public String checkout(Model model) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("items", webShopService.getCartItems());
            model.addAttribute("total", "Total: " + webShopService.getCartTotal() + " SEK");
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

    @GetMapping("/edit-profile")
    public String editProfile(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit-profile", "");
            return "shop/edit-profile";
        }
        model.addAttribute("login", "Please log in first");
        return "login";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(Model model, @Valid @ModelAttribute("customer") Customer customer, BindingResult br) {
        if (webShopService.getUser() instanceof Customer) {
            model.addAttribute("login", customer.getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
        }
        if (br.hasErrors()) {
            model.addAttribute("regcheck", "Name must be between 2 and 30 characters, password must be between 5 and 20 characters");
        } else {
            String check =  webShopService.updateUser(customer);
            model.addAttribute("edit-profile", check);
            return "shop/edit-profile";
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

    @GetMapping("/admin-edit-profile")
    public String adminEditProfile(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", webShopService.getUser().getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
            model.addAttribute("edit-profile", "");
            return "admin/admin-edit-profile";
        }
        model.addAttribute("login", "Please log in first");
        return "admin/admin-login";
    }

    @PostMapping("/admin-edit-profile")
    public String adminUpdateProfile(Model model, @Valid @ModelAttribute("employee") Employee employee, BindingResult br) {
        if (webShopService.getUser() instanceof Employee) {
            model.addAttribute("login", employee.getName());
            model.addAttribute("categories", webShopService.getCategories());
            model.addAttribute("email", webShopService.getUser().getEmail());
        }
        if (br.hasErrors()) {
            model.addAttribute("regcheck", "Name must be between 2 and 30 characters, password must be between 5 and 20 characters");
        } else {
            String check =  webShopService.adminUpdateUser(employee);
            model.addAttribute("edit-profile", check);
            return "admin/admin-edit-profile";
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
