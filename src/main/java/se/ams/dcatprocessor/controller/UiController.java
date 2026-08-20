package se.ams.dcatprocessor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiController {

    @GetMapping("/")
    public String index() {
        return "index";   // → templates/index.html
    }

    @GetMapping("/docs")
    public String editor() {
        return "docs";  // → templates/docs.html
    }
}
