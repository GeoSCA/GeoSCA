package com.cooperativa.gestioncooperativa;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cooperativa.gestioncooperativa.entity.Autobus;
import com.cooperativa.gestioncooperativa.entity.Cooperativa;
import com.cooperativa.gestioncooperativa.entity.Rol;
import com.cooperativa.gestioncooperativa.entity.Usuario;
import com.cooperativa.gestioncooperativa.repository.AutobusRepository;
import com.cooperativa.gestioncooperativa.repository.CooperativaRepository;
import com.cooperativa.gestioncooperativa.repository.ConductorRepository;
import com.cooperativa.gestioncooperativa.repository.ParroquiaRepository;
import com.cooperativa.gestioncooperativa.repository.RutaRepository;
import com.cooperativa.gestioncooperativa.repository.RolRepository;
import com.cooperativa.gestioncooperativa.repository.UsuarioRepository;
import com.cooperativa.gestioncooperativa.service.CooperativaService;
import com.cooperativa.gestioncooperativa.service.AutobusService;
import com.cooperativa.gestioncooperativa.service.ConductorService;
import com.cooperativa.gestioncooperativa.service.ParroquiaService;
import com.cooperativa.gestioncooperativa.service.RutaService;
import com.cooperativa.gestioncooperativa.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Sprint1IntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private CooperativaRepository cooperativaRepository;
    @Autowired
    private AutobusRepository autobusRepository;
    @Autowired
    private ConductorRepository conductorRepository;
    @Autowired
    private RutaRepository rutaRepository;
    @Autowired
    private ParroquiaRepository parroquiaRepository;
    @Autowired
    private CooperativaService cooperativaService;
    @Autowired
    private ParroquiaService parroquiaService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private AutobusService autobusService;
    @Autowired
    private ConductorService conductorService;
    @Autowired
    private RutaService rutaService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminCoopUsername;
    private Cooperativa propia;
    private Cooperativa ajena;

    @BeforeEach
    void prepararUsuariosYCooperativas() {
        Rol adminCooperativa = rolRepository.findByNombreIgnoreCase("ADMIN_COOPERATIVA").orElseThrow();
        propia = guardarCooperativa("Propia");
        ajena = guardarCooperativa("Ajena");
        adminCoopUsername = "coop-" + UUID.randomUUID();

        Usuario adminCoop = new Usuario();
        adminCoop.setNombre("Admin");
        adminCoop.setApellido("Cooperativa");
        adminCoop.setCorreo(adminCoopUsername + "@example.test");
        adminCoop.setUsername(adminCoopUsername);
        adminCoop.setPassword(passwordEncoder.encode("Admin12345!"));
        adminCoop.setEstado(true);
        adminCoop.setRol(adminCooperativa);
        adminCoop.setCooperativa(propia);
        usuarioRepository.save(adminCoop);
    }

    @Test
    void loginAceptaCredencialesCorrectasYRechazaIncorrectas() throws Exception {
        mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", "Admin12345!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", "incorrecta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void loginRechazaUsuarioInactivoYUsuarioDeCooperativaInactiva() throws Exception {
        Usuario usuarioInactivo = crearUsuario("disabled-" + UUID.randomUUID(), propia, false);
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", usuarioInactivo.getUsername()).param("password", "Admin12345!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        Cooperativa inactiva = guardarCooperativa("Inactiva");
        inactiva.setEstado("INACTIVA");
        cooperativaRepository.save(inactiva);
        Usuario usuarioBloqueado = crearUsuario("inactive-coop-" + UUID.randomUUID(), inactiva, true);
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", usuarioBloqueado.getUsername()).param("password", "Admin12345!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void adminCooperativaRecibe403EnModulosGenerales() throws Exception {
        var usuarioCoop = user(adminCoopUsername)
                .authorities(new SimpleGrantedAuthority("GESTIONAR_RUTAS"));
        mockMvc.perform(get("/usuarios-web").with(usuarioCoop)).andExpect(status().isForbidden());
        mockMvc.perform(post("/roles/guardar").with(user(adminCoopUsername)
                        .authorities(new SimpleGrantedAuthority("GESTIONAR_RUTAS")))
                        .with(csrf()).param("nombre", "MALICIOSO"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/cooperativas-web").with(usuarioCoop)).andExpect(status().isForbidden());
        mockMvc.perform(get("/parroquias-web").with(usuarioCoop)).andExpect(status().isForbidden());
    }

    @Test
    void adminCooperativaNoPuedeEliminarAutobusDeOtraCooperativa() throws Exception {
        Autobus autobus = new Autobus();
        autobus.setPlaca(placaUnica());
        autobus.setMarca("Marca");
        autobus.setModelo("Modelo");
        autobus.setAnio(2024);
        autobus.setCapacidad(40);
        autobus.setEstado("ACTIVO");
        autobus.setCooperativa(ajena);
        autobus = autobusRepository.save(autobus);

        mockMvc.perform(post("/autobuses/eliminar/{id}", autobus.getIdAutobus())
                        .with(user(adminCoopUsername).authorities(new SimpleGrantedAuthority("GESTIONAR_AUTOBUSES")))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/autobuses/editar/{id}", autobus.getIdAutobus())
                        .with(user(adminCoopUsername).authorities(new SimpleGrantedAuthority("GESTIONAR_AUTOBUSES"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCooperativaNoPuedeEditarNiEliminarConductorORutaAjena() throws Exception {
        var conductor = new com.cooperativa.gestioncooperativa.entity.Conductor();
        conductor.setCedula("179" + UUID.randomUUID().toString().replace("-", "").substring(0, 7));
        conductor.setNombres("Conductor");
        conductor.setApellidos("Ajeno");
        conductor.setLicencia("L-01");
        conductor.setEstado("ACTIVO");
        conductor.setCooperativa(ajena);
        conductor = conductorRepository.save(conductor);

        var origen = parroquiaRepository.findByNombreIgnoreCase("La Matriz").orElseThrow();
        var destino = parroquiaRepository.findByNombreIgnoreCase("San Buenaventura").orElseThrow();
        var ruta = new com.cooperativa.gestioncooperativa.entity.Ruta();
        ruta.setNombre("Ruta ajena " + UUID.randomUUID());
        ruta.setOrigen(origen.getNombre());
        ruta.setDestino(destino.getNombre());
        ruta.setParroquiaOrigen(origen);
        ruta.setParroquiaDestino(destino);
        ruta.setEstado("ACTIVA");
        ruta.setCooperativa(ajena);
        ruta = rutaRepository.save(ruta);

        var adminCoop = user(adminCoopUsername)
                .authorities(new SimpleGrantedAuthority("GESTIONAR_CONDUCTORES"),
                        new SimpleGrantedAuthority("GESTIONAR_RUTAS"));
        mockMvc.perform(get("/conductores/editar/{id}", conductor.getIdConductor()).with(adminCoop))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/conductores/eliminar/{id}", conductor.getIdConductor())
                        .with(adminCoop).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/rutas/editar/{id}", ruta.getIdRuta()).with(adminCoop))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/rutas/eliminar/{id}", ruta.getIdRuta()).with(adminCoop).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void renderizaDashboardFormulariosDeRolesYRutas() throws Exception {
        var admin = user("admin").authorities(
                new SimpleGrantedAuthority("GESTIONAR_ROLES"),
                new SimpleGrantedAuthority("GESTIONAR_RUTAS"),
                new SimpleGrantedAuthority("GESTIONAR_USUARIOS"));
        mockMvc.perform(get("/dashboard").with(admin)).andExpect(status().isOk());
        mockMvc.perform(get("/roles-web").with(admin)).andExpect(status().isOk());
        mockMvc.perform(get("/roles/nuevo").with(admin)).andExpect(status().isOk());
        mockMvc.perform(get("/rutas/nuevo").with(admin)).andExpect(status().isOk());
    }

    @Test
    void serviciosValidanDuplicadosFormatoYPermisos() {
        Cooperativa invalida = new Cooperativa();
        invalida.setNombre("Inválida");
        invalida.setRuc("123");
        invalida.setDireccion("Dirección");
        invalida.setTelefono("0999999999");
        invalida.setCorreo("admin@example.test");
        assertThrows(IllegalArgumentException.class, () -> cooperativaService.guardarCooperativa(invalida));

        Cooperativa correoInvalido = cooperativa("1888888888001", "correo-malo");
        assertThrows(IllegalArgumentException.class, () -> cooperativaService.guardarCooperativa(correoInvalido));
        cooperativaService.guardarCooperativa(cooperativa("1790012345001", "registrada@example.test"));
        Cooperativa duplicada = cooperativa("1790012345001", "dup@example.test");
        assertThrows(IllegalArgumentException.class, () -> cooperativaService.guardarCooperativa(duplicada));

        var parroquiaInvalida = nuevaParroquia("Prueba", "Quito", "Pichincha");
        assertThrows(IllegalArgumentException.class, () -> parroquiaService.guardarParroquia(parroquiaInvalida));
        var duplicadaParroquia = nuevaParroquia("La Matriz", "Latacunga", "Cotopaxi");
        assertThrows(IllegalArgumentException.class, () -> parroquiaService.guardarParroquia(duplicadaParroquia));

        Usuario debil = new Usuario();
        debil.setNombre("Test");
        debil.setApellido("User");
        debil.setCorreo("weak-" + UUID.randomUUID() + "@example.test");
        debil.setUsername("weak-" + UUID.randomUUID());
        debil.setPassword("short1");
        debil.setRol(rolRepository.findByNombreIgnoreCase("ADMIN_GENERAL").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardarUsuario(debil));

        Usuario duplicado = new Usuario();
        duplicado.setNombre("Test");
        duplicado.setApellido("Admin");
        duplicado.setCorreo("another-" + UUID.randomUUID() + "@example.test");
        duplicado.setUsername("admin");
        duplicado.setPassword("Admin12345!");
        duplicado.setRol(rolRepository.findByNombreIgnoreCase("ADMIN_GENERAL").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardarUsuario(duplicado));

        Usuario correoDuplicado = new Usuario();
        correoDuplicado.setNombre("Test");
        correoDuplicado.setApellido("Admin");
        correoDuplicado.setCorreo("admin@geosca.local");
        correoDuplicado.setUsername("another-" + UUID.randomUUID());
        correoDuplicado.setPassword("Admin12345!");
        correoDuplicado.setRol(rolRepository.findByNombreIgnoreCase("ADMIN_GENERAL").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardarUsuario(correoDuplicado));

        Autobus autobusInvalido = new Autobus();
        autobusInvalido.setPlaca("INVALIDA");
        assertThrows(IllegalArgumentException.class, () -> autobusService.guardarAutobus(autobusInvalido));

        Autobus capacidadInvalida = new Autobus();
        capacidadInvalida.setPlaca(placaUnica());
        capacidadInvalida.setMarca("Marca");
        capacidadInvalida.setModelo("Modelo");
        capacidadInvalida.setAnio(2024);
        capacidadInvalida.setCapacidad(0);
        capacidadInvalida.setEstado("ACTIVO");
        capacidadInvalida.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> autobusService.guardarAutobus(capacidadInvalida));

        var busDuplicado = new Autobus();
        var autobusExistente = new Autobus();
        String placa = placaUnica();
        autobusExistente.setPlaca(placa);
        autobusExistente.setMarca("Marca");
        autobusExistente.setModelo("Modelo");
        autobusExistente.setAnio(2024);
        autobusExistente.setCapacidad(30);
        autobusExistente.setEstado("ACTIVO");
        autobusExistente.setCooperativa(propia);
        autobusRepository.save(autobusExistente);
        busDuplicado.setPlaca(placa.toLowerCase());
        busDuplicado.setMarca("Marca");
        busDuplicado.setModelo("Modelo");
        busDuplicado.setAnio(2024);
        busDuplicado.setCapacidad(30);
        busDuplicado.setEstado("ACTIVO");
        busDuplicado.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> autobusService.guardarAutobus(busDuplicado));

        var conductorInvalido = new com.cooperativa.gestioncooperativa.entity.Conductor();
        conductorInvalido.setCedula("123");
        assertThrows(IllegalArgumentException.class, () -> conductorService.guardarConductor(conductorInvalido));
        var correoConductorInvalido = new com.cooperativa.gestioncooperativa.entity.Conductor();
        correoConductorInvalido.setCedula("1710034065");
        correoConductorInvalido.setNombres("Conductor");
        correoConductorInvalido.setApellidos("Correo inválido");
        correoConductorInvalido.setLicencia("L-CORREO");
        correoConductorInvalido.setFechaCaducidadLicencia(java.time.LocalDate.now().plusYears(1));
        correoConductorInvalido.setTelefono("0999999999");
        correoConductorInvalido.setCorreo("correo-invalido");
        correoConductorInvalido.setEstado("ACTIVO");
        correoConductorInvalido.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> conductorService.guardarConductor(correoConductorInvalido));

        var telefonoConductorInvalido = new com.cooperativa.gestioncooperativa.entity.Conductor();
        telefonoConductorInvalido.setCedula("1710034065");
        telefonoConductorInvalido.setNombres("Conductor");
        telefonoConductorInvalido.setApellidos("Teléfono inválido");
        telefonoConductorInvalido.setLicencia("L-TELEFONO");
        telefonoConductorInvalido.setFechaCaducidadLicencia(java.time.LocalDate.now().plusYears(1));
        telefonoConductorInvalido.setTelefono("teléfono");
        telefonoConductorInvalido.setCorreo("conductor-valido@example.test");
        telefonoConductorInvalido.setEstado("ACTIVO");
        telefonoConductorInvalido.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> conductorService.guardarConductor(telefonoConductorInvalido));

        var autobusConAnioInvalido = new Autobus();
        autobusConAnioInvalido.setPlaca(placaUnica());
        autobusConAnioInvalido.setMarca("Marca");
        autobusConAnioInvalido.setModelo("Modelo");
        autobusConAnioInvalido.setAnio(1979);
        autobusConAnioInvalido.setCapacidad(30);
        autobusConAnioInvalido.setEstado("ACTIVO");
        autobusConAnioInvalido.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> autobusService.guardarAutobus(autobusConAnioInvalido));

        var conductorCaducado = new com.cooperativa.gestioncooperativa.entity.Conductor();
        conductorCaducado.setCedula("1710034065");
        conductorCaducado.setNombres("Conductor");
        conductorCaducado.setApellidos("Caducado");
        conductorCaducado.setLicencia("L-02");
        conductorCaducado.setFechaCaducidadLicencia(java.time.LocalDate.now().minusDays(1));
        conductorCaducado.setTelefono("0999999999");
        conductorCaducado.setCorreo("caducado@example.test");
        conductorCaducado.setEstado("ACTIVO");
        conductorCaducado.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> conductorService.guardarConductor(conductorCaducado));
        var conductorDuplicado = new com.cooperativa.gestioncooperativa.entity.Conductor();
        conductorDuplicado.setCedula("1710034065");
        conductorDuplicado.setNombres("Conductor");
        conductorDuplicado.setApellidos("Duplicado");
        conductorDuplicado.setLicencia("L-01");
        conductorDuplicado.setFechaCaducidadLicencia(java.time.LocalDate.now().plusYears(1));
        conductorDuplicado.setTelefono("0999999999");
        conductorDuplicado.setCorreo("conductor@example.test");
        conductorDuplicado.setEstado("ACTIVO");
        conductorDuplicado.setCooperativa(propia);
        var existenteConductor = conductorDuplicado;
        existenteConductor.setCooperativa(ajena);
        conductorRepository.save(existenteConductor);
        conductorDuplicado.setIdConductor(null);
        conductorDuplicado.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> conductorService.guardarConductor(conductorDuplicado));

        var rutaInvalida = new com.cooperativa.gestioncooperativa.entity.Ruta();
        assertThrows(IllegalArgumentException.class, () -> rutaService.guardarRuta(rutaInvalida));

        var parroquiaOrigen = parroquiaRepository.findByNombreIgnoreCase("La Matriz").orElseThrow();
        var parroquiaDestino = parroquiaRepository.findByNombreIgnoreCase("San Buenaventura").orElseThrow();
        var ruta = new com.cooperativa.gestioncooperativa.entity.Ruta();
        ruta.setNombre("Ruta válida " + UUID.randomUUID());
        ruta.setParroquiaOrigen(parroquiaOrigen);
        ruta.setParroquiaDestino(parroquiaDestino);
        ruta.setEstado("ACTIVA");
        ruta.setCooperativa(propia);
        rutaService.guardarRuta(ruta);

        var rutaMismoOrigenDestino = new com.cooperativa.gestioncooperativa.entity.Ruta();
        rutaMismoOrigenDestino.setNombre("Ruta mismo origen " + UUID.randomUUID());
        rutaMismoOrigenDestino.setParroquiaOrigen(parroquiaOrigen);
        rutaMismoOrigenDestino.setParroquiaDestino(parroquiaOrigen);
        rutaMismoOrigenDestino.setEstado("ACTIVA");
        rutaMismoOrigenDestino.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> rutaService.guardarRuta(rutaMismoOrigenDestino));

        var rutaDistanciaInvalida = new com.cooperativa.gestioncooperativa.entity.Ruta();
        rutaDistanciaInvalida.setNombre("Ruta distancia " + UUID.randomUUID());
        rutaDistanciaInvalida.setParroquiaOrigen(parroquiaOrigen);
        rutaDistanciaInvalida.setParroquiaDestino(parroquiaDestino);
        rutaDistanciaInvalida.setDistanciaKm(0.0);
        rutaDistanciaInvalida.setEstado("ACTIVA");
        rutaDistanciaInvalida.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> rutaService.guardarRuta(rutaDistanciaInvalida));

        var rutaDuplicada = new com.cooperativa.gestioncooperativa.entity.Ruta();
        rutaDuplicada.setNombre(ruta.getNombre());
        rutaDuplicada.setParroquiaOrigen(parroquiaOrigen);
        rutaDuplicada.setParroquiaDestino(parroquiaDestino);
        rutaDuplicada.setEstado("ACTIVA");
        rutaDuplicada.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> rutaService.guardarRuta(rutaDuplicada));
    }

    @Test
    void noPermiteRetirarAlUltimoAdminGeneralActivo() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarEstado(admin.getIdUsuario(), false));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.eliminarUsuario(admin.getIdUsuario()));

        admin.setRol(rolRepository.findByNombreIgnoreCase("ADMIN_COOPERATIVA").orElseThrow());
        admin.setCooperativa(propia);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.guardarUsuario(admin));
    }

    private Usuario crearUsuario(String username, Cooperativa cooperativa, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario");
        usuario.setApellido("Prueba");
        usuario.setCorreo(username + "@example.test");
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode("Admin12345!"));
        usuario.setEstado(activo);
        usuario.setRol(rolRepository.findByNombreIgnoreCase("ADMIN_COOPERATIVA").orElseThrow());
        usuario.setCooperativa(cooperativa);
        return usuarioRepository.save(usuario);
    }

    private Cooperativa guardarCooperativa(String nombre) {
        return cooperativaRepository.save(cooperativa(
                String.format("%013d", Math.abs((long) (nombre + UUID.randomUUID()).hashCode()) + 1000000000000L)
                        .substring(0, 13),
                nombre.toLowerCase() + "-" + UUID.randomUUID() + "@example.test"));
    }

    private Cooperativa cooperativa(String ruc, String correo) {
        Cooperativa cooperativa = new Cooperativa();
        cooperativa.setNombre("Cooperativa " + UUID.randomUUID());
        cooperativa.setRuc(ruc);
        cooperativa.setDireccion("Dirección");
        cooperativa.setTelefono("0999999999");
        cooperativa.setCorreo(correo);
        cooperativa.setEstado("ACTIVA");
        return cooperativa;
    }

    private com.cooperativa.gestioncooperativa.entity.Parroquia nuevaParroquia(
            String nombre, String canton, String provincia) {
        var parroquia = new com.cooperativa.gestioncooperativa.entity.Parroquia();
        parroquia.setNombre(nombre);
        parroquia.setCanton(canton);
        parroquia.setProvincia(provincia);
        parroquia.setEstado("ACTIVA");
        return parroquia;
    }

    private String placaUnica() {
        return String.format("ABC-%04d", Math.floorMod(UUID.randomUUID().hashCode(), 10000));
    }
}
