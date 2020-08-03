package com.codegym.Casestudy.controller;

import com.codegym.Casestudy.exception.UserAlreadyExistException;
import com.codegym.Casestudy.model.user.Customer;
import com.codegym.Casestudy.service.customer.ICustomerService;
import com.codegym.Casestudy.service.product.IProductService;
import com.codegym.Casestudy.service.sku.ISkuService;
import com.codegym.Casestudy.service.role.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/home")
public class HomeController {
    @Autowired
    IProductService productService;

    @Autowired
    ISkuService skuService;

    @Autowired
    ICustomerService customerService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    IRoleService roleService;

    private String getPrincipal(){
        String userName = null;
        userName = customerService.findByMail(getMail()).getName();
        return userName;
    }

    private String getMail(){
        String mail = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            mail = ((UserDetails)principal).getUsername();
        } else {
            mail = principal.toString();
        }
        return mail;
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @GetMapping
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("productList", productService.findAll());
        modelAndView.addObject("nameUser",getPrincipal());
        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView formCreate() {
        ModelAndView modelAndView = new ModelAndView("signup");
        modelAndView.addObject("customer", new Customer());
        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView saveCustomer(@ModelAttribute("customer") @Valid Customer customer, HttpServletRequest request, Errors errors) {
        try {
            customerService.registerNewUserAccount(customer);

        } catch (UserAlreadyExistException uaeEx) {
            ModelAndView mav = new ModelAndView("signup");
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        }
            return new ModelAndView("signup", "customer", customer);
    }

    @GetMapping("/edit")
    public ModelAndView formEdit() {
        ModelAndView modelAndView = new ModelAndView("edit");
        modelAndView.addObject("customer",customerService.findByMail(getMail()));
        return modelAndView;
    }
    @PostMapping("/edit")
    public ModelAndView saveEdit(@ModelAttribute("customer") @Valid Customer customer) {
        String pwEndcode = passwordEncoder.encode(customer.getPassword());
        customer.setPassword(pwEndcode);
        customer.setRole(roleService.findByName("ROLE_USER"));
        customerService.save(customer);
        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/listuser")
    public ModelAndView listCustomer(){
        Iterable customers = customerService.findAll();
        ModelAndView modelAndView = new ModelAndView("list");
        modelAndView.addObject("customers",customers);
        return modelAndView;
    }

    @GetMapping("/listuser/edit{mail}")
    public ModelAndView showEditForm(@PathVariable String mail){
        Customer customer = customerService.findByMail(mail);
        ModelAndView modelAndView = new ModelAndView("/admin-edit");
        modelAndView.addObject("customer", customer);
        return modelAndView;
    }

    @PostMapping("/listuser/edit")
    public ModelAndView updateCustomer(@ModelAttribute("customer") Customer customer){
        customerService.save(customer);
        ModelAndView modelAndView = new ModelAndView("/admin-edit");
        modelAndView.addObject("customer", customer);
        modelAndView.addObject("message", "Customer updated successfully");
        return modelAndView;
    }

}

