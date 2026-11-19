package com.cooperativa.gestioncooperativa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import com.cooperativa.gestioncooperativa.entity.Permiso;
import com.cooperativa.gestioncooperativa.entity.Rol;
import com.cooperativa.gestioncooperativa.service.PermisoService;
import com.cooperativa.gestioncooperativa.service.RolService;

@Controller
public class RolWebController {

    private final RolService rolService;
    private final PermisoService permisoService;

    public RolWebController(RolService rolService, PermisoService permisoService) {
        this.rolService = rolService;
        this.permisoService = permisoService;
    }

    @InitBinder("rol")
    void limitarBinding(WebDataBinder binder) {
        binder.setAllowedFields("idRol", "nombre", "descripcion");
    }

    @GetMapping("/roles-web")
    public String listarRoles(Model model) {
        model.addAttribute("roles", rolService.listarRoles());
        return "roles";
    }

    @GetMapping("/roles/nuevo")
    public String nuevoRol(Model model) {
        prepararFormulario(model, new Rol());
        return "rol-form";
    }

    @PostMapping("/roles/guardar")
    public String guardarRol(
            @ModelAttribute Rol rol,
            @RequestParam(name = "permisoIds", required = false) List<Long> permisoIds,
            Model model,
            RedirectAttributes redirectAttributes) {
        rol.setPermisos(new LinkedHashSet<>());
        if (permisoIds != null) {
            permisoIds.stream().distinct().forEach(id -> {
                Permiso permiso = new Permiso();
                permiso.setIdPermiso(id);
                rol.getPermisos().add(permiso);
            });
        }
        try {
            rolService.guardarRol(rol);
            redirectAttributes.addFlashAttribute("mensaje", "Rol guardado correctamente.");
            return "redirect:/roles-web";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e instanceof IllegalArgumentException
                    ? e.getMessage() : "No se pudo guardar el rol por una restricción de datos.");
            prepararFormulario(model, rol);
            return "rol-form";
        }
    }

    @GetMapping("/roles/editar/{id}")
    public String editarRol(@PathVariable Long id, Model model) {
        Rol rol = rolService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("El rol no existe."));
        prepararFormulario(model, rol);
        return "rol-form";
    }

    @PostMapping("/roles/eliminar/{id}")
    public String eliminarRol(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rolService.eliminarRol(id);
            redirectAttributes.addFlashAttribute("mensaje", "Rol eliminado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/roles-web";
    }

    private void prepararFormulario(Model model, Rol rol) {
        model.addAttribute("rol", rol);
        model.addAttribute("permisos", permisoService.listarPermisos());
    }
}
