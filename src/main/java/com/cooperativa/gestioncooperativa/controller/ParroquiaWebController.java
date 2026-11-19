package com.cooperativa.gestioncooperativa.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cooperativa.gestioncooperativa.entity.Parroquia;
import com.cooperativa.gestioncooperativa.service.ParroquiaService;

@Controller
public class ParroquiaWebController {

    private final ParroquiaService parroquiaService;

    public ParroquiaWebController(ParroquiaService parroquiaService) {
        this.parroquiaService = parroquiaService;
    }

    @InitBinder("parroquia")
    void limitarBinding(WebDataBinder binder) {
        binder.setAllowedFields("idParroquia", "nombre", "canton", "provincia", "estado");
    }

    @GetMapping("/parroquias-web")
    public String listarParroquias(Model model) {
        model.addAttribute("parroquias", parroquiaService.listarParroquias());
        return "parroquias";
    }

    @GetMapping("/parroquias/nuevo")
    public String nuevaParroquia(Model model) {
        Parroquia parroquia = new Parroquia();
        parroquia.setCanton("Latacunga");
        parroquia.setProvincia("Cotopaxi");
        parroquia.setEstado("ACTIVA");
        model.addAttribute("parroquia", parroquia);
        return "parroquia-form";
    }

    @PostMapping("/parroquias/guardar")
    public String guardarParroquia(
            @ModelAttribute Parroquia parroquia, Model model, RedirectAttributes redirectAttributes) {
        try {
            parroquia.setCanton("Latacunga");
            parroquia.setProvincia("Cotopaxi");
            parroquiaService.guardarParroquia(parroquia);
            redirectAttributes.addFlashAttribute("mensaje", "Parroquia guardada correctamente.");
            return "redirect:/parroquias-web";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e instanceof IllegalArgumentException
                    ? e.getMessage() : "No se pudo guardar la parroquia por una restricción de datos.");
            model.addAttribute("parroquia", parroquia);
            return "parroquia-form";
        }
    }

    @GetMapping("/parroquias/editar/{id}")
    public String editarParroquia(@PathVariable Long id, Model model) {
        Parroquia parroquia = parroquiaService.obtenerPorId(id);
        if (parroquia == null) {
            throw new IllegalArgumentException("La parroquia no existe.");
        }
        model.addAttribute("parroquia", parroquia);
        return "parroquia-form";
    }

    @PostMapping("/parroquias/eliminar/{id}")
    public String eliminarParroquia(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            parroquiaService.eliminarParroquia(id);
            redirectAttributes.addFlashAttribute("mensaje", "Parroquia inactivada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/parroquias-web";
    }

    @PostMapping("/parroquias/activar/{id}")
    public String activarParroquia(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            parroquiaService.activarParroquia(id);
            redirectAttributes.addFlashAttribute("mensaje", "Parroquia activada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/parroquias-web";
    }
}
