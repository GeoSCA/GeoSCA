package com.cooperativa.gestioncooperativa.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        String nombreUsuario = username.trim();
        List<Usuario> usuarios = usuarioRepository.findAllByUsernameIgnoreCase(nombreUsuario);

        Usuario usuario = usuarios.stream()
                .filter(u -> u != null && nombreUsuario.equalsIgnoreCase(u.getUsername()))
                .sorted(Comparator.comparing(u -> Boolean.TRUE.equals(u.getEstado()) ? 0 : 1))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<SimpleGrantedAuthority> autoridades = new java.util.ArrayList<>();
        if (usuario.getRol() != null && usuario.getRol().getNombre() != null) {
            autoridades.add(new SimpleGrantedAuthority(
                    "ROLE_" + usuario.getRol().getNombre().replaceFirst("^ROLE_", "")));
            if (usuario.getRol().getPermisos() != null) {
                usuario.getRol().getPermisos().stream()
                        .map(permiso -> new SimpleGrantedAuthority(permiso.getNombre()))
                        .forEach(autoridades::add);
            }
        }

        boolean usuarioActivo = Boolean.TRUE.equals(usuario.getEstado());
        boolean esAdminCooperativa = usuario.getRol() != null
                && "ADMIN_COOPERATIVA".equalsIgnoreCase(usuario.getRol().getNombre());
        boolean cooperativaActiva = !esAdminCooperativa || esCooperativaActiva(usuario.getCooperativa());

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(autoridades)
                .disabled(!usuarioActivo)
                .accountLocked(!cooperativaActiva)
                .build();
    }

    private boolean esCooperativaActiva(Cooperativa cooperativa) {
        if (cooperativa == null) {
            return true;
        }

        String estado = cooperativa.getEstado();
        if (estado == null || estado.isBlank()) {
            return true;
        }

        String estadoNormalizado = estado.trim();
        return !("INACTIVA".equalsIgnoreCase(estadoNormalizado)
                || "INACTIVO".equalsIgnoreCase(estadoNormalizado)
                || "DESACTIVADA".equalsIgnoreCase(estadoNormalizado)
                || "DESACTIVO".equalsIgnoreCase(estadoNormalizado));
    }
}
