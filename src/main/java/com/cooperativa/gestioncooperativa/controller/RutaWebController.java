package com.cooperativa.gestioncooperativa.controller;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashSet;

import org.springframework.web.bind.annotation.RequestParam;
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

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Parroquia;
import com.cooperativa.gestioncooperativa.entity.Ruta;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.service.CooperativaService;
import com.cooperativa.gestioncooperativa.service.ParroquiaService;
import com.cooperativa.gestioncooperativa.service.RutaService;
import com.cooperativa.gestioncooperativa.service.UsuarioService;

@Controller
public class RutaWebController {

    private final RutaService rutaService;
    private final CooperativaService cooperativaService;
    private final ParroquiaService parroquiaService;
    private final UsuarioService usuarioService;

    public RutaWebController(
            RutaService rutaService,
            CooperativaService cooperativaService,
            ParroquiaService parroquiaService,
            UsuarioService usuarioService) {
        this.rutaService = rutaService;
        this.cooperativaService = cooperativaService;
        this.parroquiaService = parroquiaService;
        this.usuarioService = usuarioService;
    }

    @InitBinder("ruta")
    void limitarBinding(WebDataBinder binder) {
        binder.setAllowedFields("idRuta", "nombre", "origen", "destino",
                "parroquiaOrigen.idParroquia", "parroquiaDestino.idParroquia",
                "paradasIntermedias", "distanciaKm", "descripcion", "estado",
                "cooperativa.idCooperativa");
    }

    @GetMapping("/rutas-web")
    public String listarRutas(Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        model.addAttribute("rutas", esAdminGeneral(usuario) ? rutaService.listarRutas()
                : rutaService.listarPorCooperativa(cooperativaDelUsuario(usuario).getIdCooperativa()));
        model.addAttribute("puedeAdministrar", esAdminGeneral(usuario) || esAdminCooperativa(usuario));
        return "rutas";
    }

    @GetMapping("/rutas/nuevo")
    public String nuevaRuta(Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        Ruta ruta = new Ruta();
        ruta.setEstado("ACTIVA");
        ruta.setParroquiaOrigen(new Parroquia());
        ruta.setParroquiaDestino(new Parroquia());
        ruta.setCooperativa(esAdminGeneral(usuario) ? new Cooperativa() : cooperativaDelUsuario(usuario));
        prepararFormulario(model, ruta, usuario);
        return "ruta-form";
    }

    @PostMapping("/rutas/guardar")
    public String guardarRuta(
            @ModelAttribute Ruta ruta,
            @RequestParam(name = "paradaIds", required = false) List<Long> paradaIds,
            @RequestParam(name = "paradasFormulario", required = false) Boolean paradasFormulario,
            Model model,
            Principal principal) {
        Usuario usuario = usuarioActual(principal);
        try {
            if (ruta.getIdRuta() != null) {
                Ruta existente = obtenerRuta(ruta.getIdRuta());
                verificarCooperativa(usuario, existente.getCooperativa());
                if (paradasFormulario == null) {
                    ruta.setParadas(existente.getParadas());
                }
            }
            if (paradasFormulario != null || paradaIds != null) {
                Set<Parroquia> paradas = new LinkedHashSet<>();
                (paradaIds == null ? List.<Long>of() : paradaIds).stream()
                        .filter(Objects::nonNull).distinct().forEach(id -> {
                    Parroquia parada = new Parroquia();
                    parada.setIdParroquia(id);
                    paradas.add(parada);
                });
                ruta.setParadas(paradas);
            }
            ruta.setCooperativa(esAdminGeneral(usuario)
                    ? obtenerCooperativa(ruta.getCooperativa() == null
                            ? null : ruta.getCooperativa().getIdCooperativa())
                    : cooperativaDelUsuario(usuario));
            rutaService.guardarRuta(ruta);
            return "redirect:/rutas-web";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e instanceof IllegalArgumentException
                    ? e.getMessage() : "La ruta contiene referencias duplicadas o inválidas.");
            prepararFormulario(model, ruta, usuario);
            return "ruta-form";
        }
    }

    @GetMapping("/rutas/editar/{id}")
    public String editarRuta(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = usuarioActual(principal);
        Ruta ruta = obtenerRuta(id);
        verificarCooperativa(usuario, ruta.getCooperativa());
        if (ruta.getParroquiaOrigen() == null) {
            ruta.setParroquiaOrigen(new Parroquia());
        }
        if (ruta.getParroquiaDestino() == null) {
            ruta.setParroquiaDestino(new Parroquia());
        }
        prepararFormulario(model, ruta, usuario);
        return "ruta-form";
    }

    @PostMapping("/rutas/eliminar/{id}")
    public String eliminarRuta(
            @PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            verificarCooperativa(usuarioActual(principal), obtenerRuta(id).getCooperativa());
            rutaService.eliminarRuta(id);
            redirectAttributes.addFlashAttribute("mensaje", "Ruta eliminada correctamente.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la ruta porque tiene registros relacionados.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rutas-web";
    }

    private void prepararFormulario(Model model, Ruta ruta, Usuario usuario) {
        model.addAttribute("ruta", ruta);
        model.addAttribute("cooperativas", cooperativasPara(usuario));
        model.addAttribute("parroquias", parroquiaService.listarParroquiasActivas());
        model.addAttribute("esAdminGeneral", esAdminGeneral(usuario));
    }

    private Ruta obtenerRuta(Long id) {
        Ruta ruta = rutaService.obtenerPorId(id);
        if (ruta == null) {
            throw new IllegalArgumentException("La ruta no existe.");
        }
        return ruta;
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
            throw new AccessDeniedException("No tiene permiso para acceder a esta ruta.");
        }
    }
}
