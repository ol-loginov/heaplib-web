package com.github.ol_loginov.heaplibweb.controllers;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@RequestMapping(DashboardController.URL)
public class DashboardController {
    public static final String URL = "/";

    @GetMapping
    public String showDashboard(Model model) {
        return "dashboard";
    }
}
