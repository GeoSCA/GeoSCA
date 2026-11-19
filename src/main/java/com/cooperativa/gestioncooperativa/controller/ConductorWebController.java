package com.cooperativa.gestioncooperativa.controller;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cooperativa.gestioncooperativa.entity.Conductor;
import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.service.ConductorService;
import com.cooperativa.gestioncooperativa.service.CooperativaService;
import com.cooperativa.gestioncooperativa.service.UsuarioService;

@Controller
public class ConductorWebController {

    private final ConductorService conductorService;
    private final CooperativaService cooperativaService;
    private final UsuarioService usuarioService;

    public ConductorWebController(
            ConductorService conductorService,
            CooperativaService cooperativaService,
            UsuarioService usuarioService) {
        this.conductorService = conductorService;
        this.cooperativaService = cooperativaService;
        this.usuarioService = usuarioService;
    }

    @InitBinder("conductor")
    void limitarBinding(WebDataBinder binder) {
        binder.setAllowedFields("idConductor", "cedula", "nombres", "apellidos", "licencia",
                "fechaCaducidadLicencia", "telefono", "correo", "estado", "cooperativa.idCooperativa");
    }

    @GetMapping("/conductores-web")
    public String listarConductores(Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        model.addAttribute("conductores", esAdminGeneral(usuario) ? conductorService.listarConductores()
                : conductorService.listarPorCooperativa(cooperativaDelUsuario(usuario).getIdCooperativa()));
        model.addAttribute("puedeAdministrar", esAdminGeneral(usuario) || esAdminCooperativa(usuario));
        return "conductores";
    }

    @GetMapping("/conductores/nuevo")
    public String nuevoConductor(Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        Conductor conductor = new Conductor();
        conductor.setEstado("ACTIVO");
        conductor.setCooperativa(esAdminGeneral(usuario) ? new Cooperativa() : cooperativaDelUsuario(usuario));
        prepararFormulario(model, conductor, usuario);
        return "conductor-form";
    }

    @PostMapping("/conductores/guardar")
    public String guardarConductor(@ModelAttribute Conductor conductor, Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        try {
            if (conductor.getIdConductor() != null) {
                verificarCooperativa(usuario, obtenerConductor(conductor.getIdConductor()).getCooperativa());
            }
            conductor.setCooperativa(esAdminGeneral(usuario)
                    ? obtenerCooperativa(conductor.getCooperativa() == null
                            ? null : conductor.getCooperativa().getIdCooperativa())
                    : cooperativaDelUsuario(usuario));
            conductorService.guardarConductor(conductor);
            return "redirect:/conductores-web";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e instanceof IllegalArgumentException
                    ? e.getMessage() : "La cédula ya existe o el conductor tiene datos relacionados.");
            prepararFormulario(model, conductor, usuario);
            return "conductor-form";
        }
    }

    @GetMapping("/conductores/editar/{id}")
    public String editarConductor(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        Conductor conductor = obtenerConductor(id);
        verificarCooperativa(usuario, conductor.getCooperativa());
        prepararFormulario(model, conductor, usuario);
        return "conductor-form";
    }

    @PostMapping("/conductores/eliminar/{id}")
    public String eliminarConductor(
            @PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            verificarCooperativa(usuarioActual(principal), obtenerConductor(id).getCooperativa());
            conductorService.eliminarConductor(id);
            redirectAttributes.addFlashAttribute("mensaje", "Conductor eliminado correctamente.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el conductor porque tiene registros relacionados.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/conductores-web";
    }

    private void prepararFormulario(Model model, Conductor conductor, Usuario usuario) {
        model.addAttribute("conductor", conductor);
        model.addAttribute("cooperativas", cooperativasPara(usuario));
        model.addAttribute("esAdminGeneral", esAdminGeneral(usuario));
    }

    private Conductor obtenerConductor(Long id) {
        Conductor conductor = conductorService.obtenerPorId(id);
        if (conductor == null) {
            throw new IllegalArgumentException("El conductor no existe.");
        }
        return conductor;
    }

    private Usuario usuarioActual(Principal principal) {
        return usuarioService.buscarPorUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado ya no existe."));
    }

    private boolean esAdminGeneral(Usuario usuario) {
        return usuario.getRol() != null && "ADMIN_GENERAL".equals(usuario.getRol().getNombre());
    }

    private boolean esAdminCooperativa(Usuario usuario) {
        return usuario.getRol() != null && "ADMIN_COOPERATIVA".equals(usuario.getRol().getNombre());
    }

    private List<Cooperativa> cooperativasPara(Usuario usuario) {
        return esAdminGeneral(usuario) ? cooperativaService.listarCooperativasActivas()
                : List.of(cooperativaDelUsuario(usuario));
    }

    private Cooperativa cooperativaDelUsuario(Usuario usuario) {
        if (usuario.getCooperativa() == null || usuario.getCooperativa().getIdCooperativa() == null) {
            throw new AccessDeniedException("El usuario no tiene una cooperativa asignada.");
        }
        return usuario.getCooperativa();
    }

    private Cooperativa obtenerCooperativa(Long idCooperativa) {
        Cooperativa cooperativa = idCooperativa == null ? null : cooperativaService.obtenerPorId(idCooperativa);
        if (cooperativa == null || !cooperativaService.estaActiva(cooperativa)) {
            throw new IllegalArgumentException("La cooperativa seleccionada no existe o está inactiva.");
        }
        return cooperativa;
    }

    private void verificarCooperativa(Usuario usuario, Cooperativa cooperativa) {
        if (!esAdminGeneral(usuario)
                && (cooperativa == null || !Objects.equals(
                        cooperativaDelUsuario(usuario).getIdCooperativa(), cooperativa.getIdCooperativa()))) {
            throw new AccessDeniedException("No tiene permiso para acceder a este conductor.");
        }
    }
}
