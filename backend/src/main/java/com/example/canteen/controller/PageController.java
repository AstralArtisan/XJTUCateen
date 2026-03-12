package com.example.canteen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/windows/{id}")
    public String windowDetailPage(@PathVariable("id") Long windowId, Model model) {
        model.addAttribute("windowId", windowId);
        return "window-detail";
    }

    @GetMapping("/windows/{id}/review")
    public String reviewPage(@PathVariable("id") Long windowId, Model model) {
        model.addAttribute("windowId", windowId);
        return "review-form";
    }

    @GetMapping("/rankings")
    public String rankingPage() {
        return "ranking";
    }
}
