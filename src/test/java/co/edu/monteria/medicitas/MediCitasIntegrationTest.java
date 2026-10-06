package co.edu.monteria.medicitas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración sobre la aplicación completa: arranque del contexto, esquema
 * validado contra las entidades, autenticación JWT y autorización por rol.
 *
 * Verifica de extremo a extremo los comportamientos que antes no tenían ninguna prueba:
 * que el token identifique al usuario, que los endpoints ignoren identificadores de
 * paciente ajenos al token y que las respuestas nunca incluyan el hash de contraseña.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integración — autenticación, autorización y contrato de la API")
class MediCitasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CORREO_PACIENTE = "carlos.mendoza@email.com";
    private static final String CLAVE_PACIENTE = "Paciente123*";
    private static final String CORREO_MEDICO = "maria.gomez@medicitas.com";
    private static final String CLAVE_MEDICO = "Medico123*";
    private static final String CORREO_ADMIN = "admin@medicitas.com";
    private static final String CLAVE_ADMIN = "Admin123*";

    @Test
    @DisplayName("el contexto de aplicación arranca y el esquema coincide con las entidades")
    void contextoArranca() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    @DisplayName("los catálogos públicos responden sin token")
    void catalogosPublicosSinToken() throws Exception {
        mockMvc.perform(get("/entidades/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Medicina General"));

        mockMvc.perform(get("/entidades/ips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("IPS Salud Vital Montería - Barrio La Julia"));
    }

    @Test
    @DisplayName("los datos con tildes se cargan correctamente (el script se lee en UTF-8)")
    void acentosSeCarganBien() throws Exception {
        String cuerpo = mockMvc.perform(get("/entidades/especialidades"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(cuerpo)
                .contains("Pediatría")
                .contains("Cardiología")
                .contains("valoración")
                .doesNotContain("Ã")
                .doesNotContain("�");
    }

    @Test
    @DisplayName("un endpoint privado responde 401 sin token")
    void endpointPrivadoRequiereToken() throws Exception {
        mockMvc.perform(get("/citas/mias")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/historias-clinicas/mias")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/notificaciones")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("el login devuelve el usuario real con su rol y NUNCA el hash de contraseña")
    void loginDevuelveUsuarioSinHash() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(CORREO_PACIENTE, CLAVE_PACIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value(CORREO_PACIENTE))
                .andExpect(jsonPath("$.usuario.rol").value("ROLE_PACIENTE"))
                // Esta es la aserción clave: el hash no debe viajar en la respuesta.
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist())
                .andReturn();

        assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .doesNotContain("$2a$10$")
                .doesNotContain("passwordHash");
    }

    @Test
    @DisplayName("una contraseña incorrecta responde 401 con un mensaje genérico")
    void credencialesIncorrectasNoRevelanCuentas() throws Exception {
        MvcResult porEmail = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("nadie@email.com", "cualquiera")))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MvcResult porClave = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(CORREO_PACIENTE, "clave-mala")))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // El mismo mensaje en ambos casos: no se revela qué correos existen.
        assertThat(extraerMensaje(porEmail)).isEqualTo(extraerMensaje(porClave));
    }

    @Test
    @DisplayName("un paciente obtiene SU historia clínica, no la de otro identificador")
    void pacienteSoloAccedeASuPropiaHistoria() throws Exception {
        String token = tokenPaciente();

        mockMvc.perform(get("/historias-clinicas/mias").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diagnostico").value("Hipertensión arterial primaria"))
                .andExpect(jsonPath("$[0].contenido").isNotEmpty());

        // El identificador del paciente sale del token, así que no existe forma de
        // solicitar el historial de otro paciente por esta ruta.
        mockMvc.perform(get("/historias-clinicas/paciente/5/desde-mi-ips")
                        .header("Authorization", bearer(token))
                        .param("ipsSolicitanteId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un paciente no puede usar los endpoints de médico ni de administrador")
    void pacienteNoAccedeAOperacionesDeMedicoOAdmin() throws Exception {
        String token = tokenPaciente();

        mockMvc.perform(get("/citas/agenda").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/disponibilidades/mias").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/auth/registro-medico")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numDocumento":"1099999999","tipoDocumento":"CC",
                                 "nombreCompleto":"Médico Intruso","email":"intruso@medicitas.com",
                                 "password":"Clave123*","especialidadId":1,"ipsId":1,
                                 "registroMedico":"RM-00000-TEST"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("el alta de médicos sí funciona para el administrador")
    void administradorRegistraMedico() throws Exception {
        String token = token(CORREO_ADMIN, CLAVE_ADMIN);

        mockMvc.perform(post("/auth/registro-medico")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numDocumento":"1099887766","tipoDocumento":"CC",
                                 "nombreCompleto":"Dra. Prueba Integración","email":"prueba.integracion@medicitas.com",
                                 "password":"Temporal123*","especialidadId":2,"ipsId":1,
                                 "registroMedico":"RM-77777-TEST"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ROLE_MEDICO"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("el médico publica una franja de horario y la ve en su agenda")
    void medicoPublicaFranja() throws Exception {
        String token = tokenMedico();

        MvcResult creada = mockMvc.perform(post("/disponibilidades")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fecha":"%s","horaInicio":"14:00:00","horaFin":"14:30:00"}
                                """.formatted(manana())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("LIBRE"))
                .andExpect(jsonPath("$.medico.usuario.nombreCompleto").value("Dra. María Paula Gómez"))
                .andExpect(jsonPath("$.medico.especialidad.nombre").value("Medicina General"))
                // El nombre del profesional llega anidado; el hash no puede estarlo.
                .andExpect(jsonPath("$.medico.usuario.passwordHash").doesNotExist())
                .andReturn();

        String cuerpo = creada.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(cuerpo).doesNotContain("passwordHash");

        mockMvc.perform(get("/disponibilidades/mias").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medico.especialidad.nombre").value("Medicina General"));
    }

    @Test
    @DisplayName("no se puede publicar una franja con la hora de fin antes que la de inicio")
    void franjaConHorariosInvertidosRechazada() throws Exception {
        String token = tokenMedico();

        mockMvc.perform(post("/disponibilidades")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fecha":"%s","horaInicio":"15:00:00","horaFin":"14:00:00"}
                                """.formatted(manana())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("anterior")));
    }

    @Test
    @DisplayName("el paciente agenda sobre una franja libre y la consulta en 'mis citas'")
    void pacienteAgendaCita() throws Exception {
        String token = tokenPaciente();

        // Se crea una franja nueva para este test, con su propio médico.
        String tokenAdmin = token(CORREO_ADMIN, CLAVE_ADMIN);
        mockMvc.perform(post("/auth/registro-medico")
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numDocumento":"1099777655","tipoDocumento":"CC",
                                 "nombreCompleto":"Dr. Prueba Agenda","email":"prueba.agenda@medicitas.com",
                                 "password":"Temporal123*","especialidadId":2,"ipsId":1,
                                 "registroMedico":"RM-66666-TEST"}
                                """))
                .andExpect(status().isCreated());

        String tokenMedicoNuevo = token("prueba.agenda@medicitas.com", "Temporal123*");
        MvcResult franja = mockMvc.perform(post("/disponibilidades")
                        .header("Authorization", bearer(tokenMedicoNuevo))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fecha":"%s","horaInicio":"16:00:00","horaFin":"16:30:00"}
                                """.formatted(manana())))
                .andExpect(status().isCreated())
                .andReturn();

        long disponibilidadId = objectMapper.readTree(franja.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("id").asLong();

        mockMvc.perform(post("/citas/agendar")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"disponibilidadId":%d,"tipo":"CONSULTA","observaciones":"Primera consulta"}
                                """.formatted(disponibilidadId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.paciente.email").value(CORREO_PACIENTE))
                .andExpect(jsonPath("$.disponibilidad.estado").value("OCUPADO"))
                .andExpect(jsonPath("$.paciente.passwordHash").doesNotExist());

        mockMvc.perform(get("/citas/mias").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$[0].disponibilidad.medico.especialidad.nombre").isNotEmpty());
    }

    @Test
    @DisplayName("el paciente ve sus notificaciones y puede marcar una como leída")
    void pacienteGestionaNotificaciones() throws Exception {
        String token = tokenPaciente();

        MvcResult bandeja = mockMvc.perform(get("/notificaciones").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        String json = bandeja.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(json).doesNotContain("passwordHash");

        JsonNode primera = objectMapper.readTree(json).path(0);
        if (!primera.isMissingNode()) {
            long id = primera.get("id").asLong();
            assertThat(primera.get("usuario")).isNull();

            mockMvc.perform(put("/notificaciones/{id}/marcar-leida", id)
                            .header("Authorization", bearer(token)))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    @DisplayName("la verificación BDUA rechaza una EPS sin convenio en la región")
    void bduaBloqueaEpsSinConvenio() throws Exception {
        mockMvc.perform(get("/auth/validar-bdua")
                        .param("tipoDocumento", "CC")
                        .param("numDocumento", "1067890128"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.afiliadoActivo").value(true))
                .andExpect(jsonPath("$.epsSoportadaEnRegion").value(false))
                .andExpect(jsonPath("$.mensajeOrientacion").isNotEmpty());

        mockMvc.perform(get("/auth/validar-bdua")
                        .param("tipoDocumento", "CC")
                        .param("numDocumento", "1067890124"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.afiliadoActivo").value(true))
                .andExpect(jsonPath("$.epsSoportadaEnRegion").value(true));
    }

    // ------------------------------------------------------------------

    private String tokenPaciente() throws Exception {
        return token(CORREO_PACIENTE, CLAVE_PACIENTE);
    }

    private String tokenMedico() throws Exception {
        return token(CORREO_MEDICO, CLAVE_MEDICO);
    }

    private String token(String correo, String clave) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(correo, clave)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("token").asText();
    }

    private String credenciales(String correo, String clave) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(correo, clave);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String extraerMensaje(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("mensaje").asText();
    }

    private String manana() {
        return java.time.LocalDate.now().plusDays(1).toString();
    }
}
