package com.cooperativa.gestioncooperativa.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Parroquia;
import com.cooperativa.gestioncooperativa.entity.Permiso;
import com.cooperativa.gestioncooperativa.entity.Rol;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.repository.ParroquiaRepository;
import com.cooperativa.gestioncooperativa.repository.PermisoRepository;
import com.cooperativa.gestioncooperativa.repository.RolRepository;
import com.cooperativa.gestioncooperativa.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Map<String, String> PERMISOS = Map.of(
            "GESTIONAR_USUARIOS", "Crear, editar y administrar usuarios",
            "GESTIONAR_ROLES", "Administrar roles y permisos",
            "GESTIONAR_COOPERATIVAS", "Administrar cooperativas",
            "GESTIONAR_PARROQUIAS", "Administrar parroquias",
            "GESTIONAR_RUTAS", "Administrar rutas",
            "GESTIONAR_AUTOBUSES", "Administrar autobuses",
            "GESTIONAR_CONDUCTORES", "Administrar conductores",
            "CONSULTAR_TRANSPORTE", "Consultar módulos de transporte");

    // Lista editable de parroquias urbanas y rurales conforme al registro oficial local.
    private static final List<String> PARROQUIAS_LATACUNGA = List.of(
            "Eloy Alfaro", "Ignacio Flores", "Juan Montalvo", "La Matriz", "San Buenaventura",
            "Aláquez", "Belisario Quevedo", "Guaytacama", "Joseguango Bajo", "Mulaló",
            "11 de Noviembre", "Poaló", "San Juan de Pastocalle", "Tanicuchí", "Toacaso");

    private static final Set<String> TODOS = Set.copyOf(PERMISOS.keySet());
    private static final Set<String> TRANSPORTE = Set.of(
            "CONSULTAR_TRANSPORTE", "GESTIONAR_RUTAS", "GESTIONAR_AUTOBUSES", "GESTIONAR_CONDUCTORES");

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParroquiaRepository parroquiaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public DataInitializer(
            RolRepository rolRepository,
            PermisoRepository permisoRepository,
            UsuarioRepository usuarioRepository,
            ParroquiaRepository parroquiaRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.password}") String adminPassword) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.usuarioRepository = usuarioRepository;
        this.parroquiaRepository = parroquiaRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Permiso> permisos = crearPermisos();
        Rol adminGeneral = obtenerOCrearRol("ADMIN_GENERAL", "Administrador general del sistema");
        Rol adminCooperativa = obtenerOCrearRol("ADMIN_COOPERATIVA", "Administrador de una cooperativa");

        asignarPermisos(adminGeneral, permisos, TODOS);
        asignarPermisos(adminCooperativa, permisos, TRANSPORTE);
        rolRepository.save(adminGeneral);
        rolRepository.save(adminCooperativa);

        if (usuarioRepository.findAllByUsernameIgnoreCase("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setApellido("General");
            admin.setCorreo("admin@geosca.local");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEstado(true);
            admin.setRol(adminGeneral);
            usuarioRepository.save(admin);
        }

        sembrarParroquias();
    }

    private Map<String, Permiso> crearPermisos() {
        var resultado = new java.util.LinkedHashMap<String, Permiso>();
        PERMISOS.forEach((nombre, descripcion) -> {
            Permiso permiso = permisoRepository.findByNombre(nombre).orElseGet(() -> {
                Permiso nuevo = new Permiso();
                nuevo.setNombre(nombre);
                nuevo.setDescripcion(descripcion);
                return permisoRepository.save(nuevo);
            });
            resultado.put(nombre, permiso);
        });
        return resultado;
    }

    private Rol obtenerOCrearRol(String nombre, String descripcion) {
        return rolRepository.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            rol.setPermisos(new LinkedHashSet<>());
            return rolRepository.save(rol);
        });
    }

    private void asignarPermisos(Rol rol, Map<String, Permiso> todos, Set<String> asignados) {
        if (rol.getPermisos() == null) {
            rol.setPermisos(new LinkedHashSet<>());
        }
        asignados.forEach(nombre -> rol.getPermisos().add(todos.get(nombre)));
    }

    private void sembrarParroquias() {
        PARROQUIAS_LATACUNGA.forEach(nombre -> {
            if (parroquiaRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
                Parroquia parroquia = new Parroquia();
                parroquia.setNombre(nombre);
                parroquia.setCanton("Latacunga");
                parroquia.setProvincia("Cotopaxi");
                parroquia.setEstado("ACTIVA");
                parroquiaRepository.save(parroquia);
            }
        });
    }
}
