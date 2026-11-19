package com.cooperativa.gestioncooperativa.controller;

import java.security.Principal;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
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

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Rol;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.service.CooperativaService;
import com.cooperativa.gestioncooperativa.service.RolService;
import com.cooperativa.gestioncooperativa.service.UsuarioService;

@Controller
public class UsuarioWebController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String[] PALABRAS_PASSWORD_TEMPORAL = {
            "Ruta", "Buses", "Cotopaxi", "Latacunga", "Parada", "Viaje",
            "Camino", "Volcan", "Andes", "Terminal", "Pasaje", "Destino"
    };

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final CooperativaService cooperativaService;

    public UsuarioWebController(
            UsuarioService usuarioService,
            RolService rolService,
            CooperativaService cooperativaService) {
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.cooperativaService = cooperativaService;
    }

    @InitBinder("usuario")
    void limitarBinding(WebDataBinder binder) {
        binder.setAllowedFields("idUsuario", "nombre", "apellido", "correo", "username", "password",
                "rol.idRol", "cooperativa.idCooperativa");
    }

    @GetMapping("/usuarios-web")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        Usuario usuario = new Usuario();
        usuario.setCooperativa(new Cooperativa());
        usuario.setEstado(true);
        prepararFormulario(model, usuario);
        return "usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (usuario.getIdUsuario() != null) {
                Usuario usuarioGuardado = usuarioService.obtenerPorId(usuario.getIdUsuario());
                if (usuarioGuardado != null && usuarioGuardado.getRol() != null
                        && "ADMIN_GENERAL".equalsIgnoreCase(usuarioGuardado.getRol().getNombre())) {
                    redirectAttributes.addFlashAttribute("error", "El administrador general no se puede editar.");
                    return "redirect:/usuarios-web";
                }
            }
            if (usuario.getRol() != null && usuario.getRol().getIdRol() != null) {
                Rol rolSeleccionado = rolService.obtenerPorId(usuario.getRol().getIdRol()).orElse(null);
                if (rolSeleccionado != null && "ADMIN_GENERAL".equalsIgnoreCase(rolSeleccionado.getNombre())) {
                    model.addAttribute("error",
                            "No se puede asignar el rol ADMIN_GENERAL. Solo existe un administrador general.");
                    prepararFormulario(model, usuario);
                    return "usuario-form";
                }
            }
            boolean usuarioNuevo = usuario.getIdUsuario() == null;
            String username = null;
            String passwordTemporal = null;
            if (usuarioNuevo) {
                usuario.setEstado(true);
                username = generarUsername(usuario);
                passwordTemporal = generarPasswordTemporal();
                usuario.setUsername(username);
                usuario.setPassword(passwordTemporal);
            }
            usuarioService.guardarUsuario(usuario);
            String mensaje = usuarioNuevo
                    ? "Usuario guardado correctamente. Usuario: " + username
                            + " | Contraseña temporal: " + passwordTemporal
                    : "Usuario guardado correctamente.";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            return "redirect:/usuarios-web";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error", e instanceof IllegalArgumentException
                    ? e.getMessage() : "El username o correo ya existe o viola una restricción de datos.");
            prepararFormulario(model, usuario);
            return "usuario-form";
        }
    }

    private String generarUsername(Usuario usuario) {
        String nombre = usuario.getNombre();
        String primeraPalabra = nombre == null ? "" : nombre.trim().split("\\s+", 2)[0];
        String prefijo = normalizarUsername(primeraPalabra);
        String username = prefijo;
        int sufijo = 2;
        while (!prefijo.isEmpty() && usuarioService.buscarPorUsername(username).isPresent()) {
            username = prefijo + sufijo++;
        }
        return username;
    }

    private String normalizarUsername(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private String generarPasswordTemporal() {
        String palabra = PALABRAS_PASSWORD_TEMPORAL[SECURE_RANDOM.nextInt(PALABRAS_PASSWORD_TEMPORAL.length)];
        String digitos = String.format(Locale.ROOT, "%04d", SECURE_RANDOM.nextInt(10_000));
        return palabra + digitos;
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario != null && usuario.getRol() != null
                && "ADMIN_GENERAL".equalsIgnoreCase(usuario.getRol().getNombre())) {
            redirectAttributes.addFlashAttribute("error",
                    "El administrador general no se puede desactivar ni eliminar.");
            return "redirect:/usuarios-web";
        }
        if (usuario != null && usuario.getUsername() != null
                && usuario.getUsername().equalsIgnoreCase(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "No puede eliminar su propio usuario.");
            return "redirect:/usuarios-web";
        }
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios-web";
    }

    @PostMapping("/usuarios/estado/{id}")
    public String cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam boolean activo,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario != null && usuario.getRol() != null
                && "ADMIN_GENERAL".equalsIgnoreCase(usuario.getRol().getNombre())) {
            redirectAttributes.addFlashAttribute("error",
                    "El administrador general no se puede desactivar ni eliminar.");
            return "redirect:/usuarios-web";
        }
        if (!activo && usuario != null && usuario.getUsername() != null
                && usuario.getUsername().equalsIgnoreCase(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "No puede desactivar su propio usuario.");
            return "redirect:/usuarios-web";
        }
        try {
            usuarioService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("mensaje", "Estado del usuario actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios-web";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        if (usuario.getRol() != null && "ADMIN_GENERAL".equalsIgnoreCase(usuario.getRol().getNombre())) {
            redirectAttributes.addFlashAttribute("error", "El administrador general no se puede editar.");
            return "redirect:/usuarios-web";
        }
        if (usuario.getCooperativa() == null) {
            usuario.setCooperativa(new Cooperativa());
        }
        prepararFormulario(model, usuario);
        return "usuario-form";
    }

    private void prepararFormulario(Model model, Usuario usuario) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.listarRoles().stream()
                .filter(rol -> !"ADMIN_GENERAL".equalsIgnoreCase(rol.getNombre()))
                .toList());
        model.addAttribute("cooperativas", cooperativaService.listarCooperativasActivas());
    }
}
