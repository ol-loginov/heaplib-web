package com.github.ol_loginov.heaplibweb.controllers;

import com.github.ol_loginov.heaplibweb.services.InputFilesManager;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

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
		model.addAttribute("inputFilesFolder", inputFilesManager.getInputFilesFolder());
		model.addAttribute("inputFiles", inputFilesManager.listInputFiles());
		model.addAttribute("inputLoads", inputFilesManager.listLoadedFiles());
		return "inputs";
	}

	@PostMapping("/load")
	public String loadRelativeFIle(@RequestParam String path) {
		inputFilesManager.createLoad(path);
		return UrlBasedViewResolver.REDIRECT_URL_PREFIX + URL;
	}
}
