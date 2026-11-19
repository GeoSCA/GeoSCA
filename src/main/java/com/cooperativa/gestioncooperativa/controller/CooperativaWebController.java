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

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.service.CooperativaService;

@Controller
public class CooperativaWebController {

    private final CooperativaService cooperativaService;

    public CooperativaWebController(CooperativaService cooperativaService) {
        this.cooperativaService = cooperativaService;
    }

    @InitBinder("cooperativa")
    void limitarBinding(WebDataBinder binder) {
        binder.setAllowedFields("idCooperativa", "nombre", "ruc", "direccion", "telefono", "correo", "estado");
    }

    @GetMapping("/cooperativas-web")
    public String listarCooperativas(Model model) {
        model.addAttribute("cooperativas", cooperativaService.listarCooperativas());
        return "cooperativas";
    }

    @GetMapping("/cooperativas/nuevo")
    public String nuevaCooperativa(Model model) {
        model.addAttribute("cooperativa", new Cooperativa());
        return "cooperativa-form";
    }

    @PostMapping("/cooperativas/guardar")
    public String guardarCooperativa(
            @ModelAttribute Cooperativa cooperativa, Model model, RedirectAttributes redirectAttributes) {
        try {
            cooperativaService.guardarCooperativa(cooperativa);
            redirectAttributes.addFlashAttribute("mensaje", "Cooperativa guardada correctamente.");
            return "redirect:/cooperativas-web";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", mensaje(e));
            model.addAttribute("cooperativa", cooperativa);
            return "cooperativa-form";
        }
    }

    @GetMapping("/cooperativas/editar/{id}")
    public String editarCooperativa(@PathVariable Long id, Model model) {
        Cooperativa cooperativa = cooperativaService.obtenerPorId(id);
        if (cooperativa == null) {
            throw new IllegalArgumentException("La cooperativa no existe.");
        }
        model.addAttribute("cooperativa", cooperativa);
        return "cooperativa-form";
    }

    @PostMapping("/cooperativas/inactivar/{id}")
    public String inactivarCooperativa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cooperativaService.inactivarCooperativa(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cooperativa inactivada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cooperativas-web";
    }

    @PostMapping("/cooperativas/activar/{id}")
    public String activarCooperativa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            cooperativaService.activarCooperativa(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cooperativa activada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cooperativas-web";
    }

    private String mensaje(Exception exception) {
        return exception instanceof IllegalArgumentException
                ? exception.getMessage()
                : "No se pudo guardar la cooperativa por una restricción de datos.";
    }
}
