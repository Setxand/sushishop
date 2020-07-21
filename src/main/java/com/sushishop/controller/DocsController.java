package com.sushishop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {

	@GetMapping("/test/docs")
	public String getDocumentation() {
		return "documentation";
	}
}
