package com.cooperativa.gestioncooperativa.controller;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.repository.UsuarioRepository;
import com.cooperativa.gestioncooperativa.service.UsuarioService;

@Controller
public class CuentaController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaController(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cuenta/password")
    public String mostrarFormulario(Principal principal) {
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("El usuario autenticado ya no existe."));
        if (usuario.getRol() != null && "ADMIN_GENERAL".equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw new AccessDeniedException(
                    "El administrador general no puede cambiar su contraseña desde la aplicación.");
        }
        return "cambiar-password";
    }

    @PostMapping("/cuenta/password")
    public String cambiarPassword(
            @RequestParam(name = "passwordActual", required = false) String passwordActual,
            @RequestParam(name = "nuevaPassword", required = false) String nuevaPassword,
            @RequestParam(name = "confirmarNuevaPassword", required = false) String confirmarNuevaPassword,
            Principal principal,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        Usuario usuario = usuarioService.buscarPorUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("El usuario autenticado ya no existe."));
        if (usuario.getRol() != null && "ADMIN_GENERAL".equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw new AccessDeniedException(
                    "El administrador general no puede cambiar su contraseña desde la aplicación.");
        }

        if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            model.addAttribute("error", "La contraseña actual es incorrecta.");
            return "cambiar-password";
        }
        if (nuevaPassword == null || !nuevaPassword.equals(confirmarNuevaPassword)) {
            model.addAttribute("error", "La nueva contraseña y su confirmación no coinciden.");
            return "cambiar-password";
        }
        if (nuevaPassword.equals(passwordActual)) {
            model.addAttribute("error", "La nueva contraseña debe ser distinta de la actual.");
            return "cambiar-password";
        }
        if (!esPasswordValida(nuevaPassword)) {
            model.addAttribute("error", "La nueva contraseña debe tener al menos 8 caracteres, una letra y un número.");
            return "cambiar-password";
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?logout";
    }

    private boolean esPasswordValida(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    }
}
