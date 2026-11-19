package com.cooperativa.gestioncooperativa.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Rol;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.repository.CooperativaRepository;
import com.cooperativa.gestioncooperativa.repository.RolRepository;
import com.cooperativa.gestioncooperativa.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CooperativaRepository cooperativaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            CooperativaRepository cooperativaRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.cooperativaRepository = cooperativaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }

        validarCamposObligatorios(usuario);
        Usuario usuarioExistente = usuario.getIdUsuario() == null ? null
                : usuarioRepository.findById(usuario.getIdUsuario())
                        .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        usuario.setRol(obtenerRol(usuario));
        usuario.setCooperativa(obtenerCooperativa(usuario));

        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio para el usuario.");
        }

        if ("ADMIN_COOPERATIVA".equalsIgnoreCase(usuario.getRol().getNombre())
                && (usuario.getCooperativa() == null || usuario.getCooperativa().getIdCooperativa() == null)) {
            throw new IllegalArgumentException("Un administrador de cooperativa debe tener una cooperativa asignada.");
        }

        if (usuario.getCooperativa() != null && usuario.getCooperativa().getIdCooperativa() != null) {
            if (!cooperativaRepository.findById(usuario.getCooperativa().getIdCooperativa()).map(this::cooperativaActiva).orElse(false)) {
                throw new IllegalArgumentException("La cooperativa asignada no está activa.");
            }
        }

        if (usuario.getIdUsuario() == null) {
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                throw new IllegalArgumentException("La contraseña es obligatoria para crear un usuario.");
            }
            validarPassword(usuario.getPassword());
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        } else {
            if (usuario.getEstado() == null) {
                usuario.setEstado(usuarioExistente.getEstado());
            }
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                usuario.setPassword(usuarioExistente.getPassword());
            } else {
                validarPassword(usuario.getPassword());
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
            if (esAdminGeneralActivo(usuarioExistente)
                    && (!Boolean.TRUE.equals(usuario.getEstado()) || !esRolAdminGeneral(usuario.getRol()))) {
                validarPuedeRetirarAdmin(usuarioExistente);
            }
        }

        validarDuplicados(usuario);

        return usuarioRepository.save(usuario);
    }

    private void validarCamposObligatorios(Usuario usuario) {
        String nombre = normalizarTexto(usuario.getNombre());
        String apellido = normalizarTexto(usuario.getApellido());
        String correo = normalizarTexto(usuario.getCorreo()).toLowerCase(Locale.ROOT);
        String username = normalizarTexto(usuario.getUsername()).toLowerCase(Locale.ROOT);
        if (nombre.isBlank() || apellido.isBlank() || correo.isBlank() || username.isBlank()) {
            throw new IllegalArgumentException("Nombre, apellido, correo y username son obligatorios.");
        }

        if (!correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setUsername(username);
    }

    private void validarDuplicados(Usuario usuario) {
        String username = normalizarTexto(usuario.getUsername());
        String correo = normalizarTexto(usuario.getCorreo());

        List<Usuario> usuariosPorUsername = usuarioRepository.findAllByUsernameIgnoreCase(username);
        boolean usernameDuplicado = usuariosPorUsername.stream()
                .anyMatch(u -> !Objects.equals(u.getIdUsuario(), usuario.getIdUsuario()));
        if (usernameDuplicado) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado.");
        }

        List<Usuario> usuariosPorCorreo = usuarioRepository.findAllByCorreoIgnoreCase(correo);
        boolean correoDuplicado = usuariosPorCorreo.stream()
                .anyMatch(u -> !Objects.equals(u.getIdUsuario(), usuario.getIdUsuario()));
        if (correoDuplicado) {
            throw new IllegalArgumentException("El correo ya está registrado en otro usuario.");
        }
    }

    private Rol obtenerRol(Usuario usuario) {
        if (usuario.getRol() == null || usuario.getRol().getIdRol() == null) {
            return null;
        }
        return rolRepository.findById(usuario.getRol().getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no existe."));
    }

    private Cooperativa obtenerCooperativa(Usuario usuario) {
        if (usuario.getCooperativa() == null || usuario.getCooperativa().getIdCooperativa() == null) {
            return null;
        }
        return cooperativaRepository.findById(usuario.getCooperativa().getIdCooperativa())
                .orElseThrow(() -> new IllegalArgumentException("La cooperativa seleccionada no existe."));
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (esAdminGeneralActivo(usuario)) {
            validarPuedeRetirarAdmin(usuario);
        }
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (!activo && esAdminGeneralActivo(usuario)) {
            validarPuedeRetirarAdmin(usuario);
        }
        usuario.setEstado(activo);
        usuarioRepository.save(usuario);
    }

    public boolean esUltimoAdministradorGeneralActivo(Usuario usuario) {
        if (!esAdminGeneralActivo(usuario)) {
            return false;
        }
        long administradoresActivos = usuarioRepository.findAll().stream()
                .filter(this::esAdminGeneralActivo)
                .count();
        return administradoresActivos <= 1;
    }

    private void validarPuedeRetirarAdmin(Usuario usuario) {
        if (esUltimoAdministradorGeneralActivo(usuario)) {
            throw new IllegalArgumentException("No se puede desactivar, eliminar o cambiar el rol del último ADMIN_GENERAL activo.");
        }
    }

    private boolean esAdminGeneralActivo(Usuario usuario) {
        return usuario != null && Boolean.TRUE.equals(usuario.getEstado()) && esRolAdminGeneral(usuario.getRol());
    }

    private boolean esRolAdminGeneral(Rol rol) {
        return rol != null && "ADMIN_GENERAL".equalsIgnoreCase(rol.getNombre());
    }

    private void validarPassword(String password) {
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una letra y un número.");
        }
    }

    private boolean cooperativaActiva(Cooperativa cooperativa) {
        if (cooperativa == null) {
            return false;
        }
        String estado = normalizarTexto(cooperativa.getEstado());
        return estado.isEmpty() || "ACTIVA".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado);
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }
}