package com.cooperativa.gestioncooperativa.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        model.addAttribute("permisos", authorities);
        model.addAttribute("esAdminGeneral", authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN_GENERAL".equalsIgnoreCase(authority.getAuthority())));
        return "dashboard";
    }
}
