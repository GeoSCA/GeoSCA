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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cooperativa.gestioncooperativa.entity.Autobus;
import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.service.AutobusService;
import com.cooperativa.gestioncooperativa.service.CooperativaService;
import com.cooperativa.gestioncooperativa.service.UsuarioService;

@Controller
public class AutobusWebController {

    private final AutobusService autobusService;
    private final CooperativaService cooperativaService;
    private final UsuarioService usuarioService;

    public AutobusWebController(
            AutobusService autobusService, CooperativaService cooperativaService, UsuarioService usuarioService) {
        this.autobusService = autobusService;
        this.cooperativaService = cooperativaService;
        this.usuarioService = usuarioService;
    }

    @InitBinder("autobus")
    void limitarBinding(WebDataBinder binder) {
        binder.setAllowedFields("idAutobus", "placa", "marca", "modelo", "anio", "capacidad", "estado",
                "cooperativa.idCooperativa");
    }

    @GetMapping("/autobuses-web")
    public String listarAutobuses(Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        model.addAttribute("autobuses", esAdminGeneral(usuario) ? autobusService.listarAutobuses()
                : autobusService.listarPorCooperativa(cooperativaDelUsuario(usuario).getIdCooperativa()));
        model.addAttribute("puedeAdministrar", esAdminGeneral(usuario) || esAdminCooperativa(usuario));
        return "autobuses";
    }

    @GetMapping("/autobuses/nuevo")
    public String nuevoAutobus(Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        Autobus autobus = new Autobus();
        autobus.setEstado("ACTIVO");
        autobus.setCooperativa(esAdminGeneral(usuario) ? new Cooperativa() : cooperativaDelUsuario(usuario));
        prepararFormulario(model, autobus, usuario);
        return "autobus-form";
    }

    @PostMapping("/autobuses/guardar")
    public String guardarAutobus(@ModelAttribute Autobus autobus, Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        try {
            if (autobus.getIdAutobus() != null) {
                verificarCooperativa(usuario, obtenerAutobus(autobus.getIdAutobus()).getCooperativa());
            }
            autobus.setCooperativa(esAdminGeneral(usuario)
                    ? obtenerCooperativa(autobus.getCooperativa() == null
                            ? null : autobus.getCooperativa().getIdCooperativa())
                    : cooperativaDelUsuario(usuario));
            autobusService.guardarAutobus(autobus);
            return "redirect:/autobuses-web";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e instanceof IllegalArgumentException
                    ? e.getMessage() : "La placa ya existe o el autobús tiene datos relacionados.");
            prepararFormulario(model, autobus, usuario);
            return "autobus-form";
        }
    }

    @GetMapping("/autobuses/editar/{id}")
    public String editarAutobus(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        Autobus autobus = obtenerAutobus(id);
        verificarCooperativa(usuario, autobus.getCooperativa());
        prepararFormulario(model, autobus, usuario);
        return "autobus-form";
    }

    @PostMapping("/autobuses/eliminar/{id}")
    public String eliminarAutobus(
            @PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            verificarCooperativa(usuarioActual(principal), obtenerAutobus(id).getCooperativa());
            autobusService.eliminarAutobus(id);
            redirectAttributes.addFlashAttribute("mensaje", "Autobús eliminado correctamente.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el autobús porque tiene registros relacionados.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/autobuses-web";
    }

    @PostMapping("/autobuses/estado/{id}")
    public String actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            verificarCooperativa(usuarioActual(principal), obtenerAutobus(id).getCooperativa());
            autobusService.actualizarEstado(id, estado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado del autobús actualizado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/autobuses-web";
    }

    private void prepararFormulario(Model model, Autobus autobus, Usuario usuario) {
        model.addAttribute("autobus", autobus);
        model.addAttribute("cooperativas", cooperativasPara(usuario));
        model.addAttribute("esAdminGeneral", esAdminGeneral(usuario));
    }

    private Autobus obtenerAutobus(Long id) {
        Autobus autobus = autobusService.obtenerPorId(id);
        if (autobus == null) {
            throw new IllegalArgumentException("El autobús no existe.");
        }
        return autobus;
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
            throw new AccessDeniedException("No tiene permiso para acceder a este autobús.");
        }
    }
}
