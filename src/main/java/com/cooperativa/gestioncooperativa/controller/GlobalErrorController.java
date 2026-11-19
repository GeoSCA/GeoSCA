package com.cooperativa.gestioncooperativa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class GlobalErrorController {

    @GetMapping("/403")
    @ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    public String forbidden(Model model) {
        model.addAttribute("codigo", 403);
        model.addAttribute("mensaje", "No tiene permisos para realizar esta operación.");
        return "error";
    }

}
