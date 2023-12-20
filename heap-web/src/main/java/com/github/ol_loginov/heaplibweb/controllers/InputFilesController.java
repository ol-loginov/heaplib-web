package com.github.ol_loginov.heaplibweb.controllers;

import com.github.ol_loginov.heaplibweb.services.InputFilesManager;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping(InputFilesController.URL)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class InputFilesController {
    public static final String URL = "/inputs";

    private final InputFilesManager inputFilesManager;

    @GetMapping
    public String showDashboard(Model model) throws IOException {
        model.addAttribute("inputFiles", inputFilesManager.listInputFiles());
        return "inputs";
    }
}
