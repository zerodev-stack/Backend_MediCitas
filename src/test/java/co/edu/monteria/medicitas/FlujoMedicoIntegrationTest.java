package co.edu.monteria.medicitas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo del médico y su efecto en el lado del paciente:
 * perfil -> servicios y sedes -> bloques -> cola de espera -> asignación -> cancelaciones.
 *
 * Usa al médico de Cardiología (Clínica Montería Central) de la semilla. El contexto se
 * reinicia antes de la clase para no heredar datos de otras pruebas que comparten la
 * misma base H2 en memoria.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("Integración — flujo del médico enlazado con el paciente")
class FlujoMedicoIntegrationTest {

    private static final long CARDIOLOGIA = 3L;
    private static final long CLINICA_CENTRAL = 2L;
    private static final long CONSULTA_CARDIOLOGIA = 6L;
    private static final long ELECTROCARDIOGRAMA = 7L;
    private static final long SEDE_CENTRO_CLINICA = 3L;
    private static final long SEDE_NORTE_CLINICA = 4L;
    private static final long SEDE_DE_OTRA_IPS = 1L;
    private static final long SERVICIO_DE_OTRA_ESPECIALIDAD = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("perfil, servicios, bloques, cola y cancelaciones se reflejan en el paciente")
    void flujoCompleto() throws Exception {
        String medico = token("jorge.rivera@medicitas.com", "Medico123*");
        String carlos = token("carlos.mendoza@email.com", "Paciente123*");
        String andres = token("andres.torres@email.com", "Paciente123*");

        // --- Perfil ------------------------------------------------------------
        JsonNode perfil = json(llamar(get("/medicos/me"), medico, null, status().isOk()));
        assertThat(perfil.path("especialidad").path("nombre").asText()).isEqualTo("Cardiología");
        assertThat(perfil.path("ciudadAtencion").asText()).isEqualTo("Montería, Córdoba");
        assertThat(perfil.path("servicios")).hasSize(3);

        perfil = json(llamar(put("/medicos/me"), medico, """
                {"ciudadAtencion":"Cereté, Córdoba","correoContacto":"","telefonoContacto":"3001112233"}
                """, status().isOk()));
        assertThat(perfil.path("ciudadAtencion").asText()).isEqualTo("Cereté, Córdoba");
        assertThat(perfil.path("telefonoContacto").asText()).isEqualTo("3001112233");
        assertThat(perfil.path("correoContacto").isNull()).isTrue();

        llamar(put("/medicos/me"), medico, """
                {"ciudadAtencion":"Montería, Córdoba","correoContacto":"no-es-correo"}
                """, status().isBadRequest());

        // --- Servicios y sedes -------------------------------------------------
        JsonNode catalogo = json(llamar(get("/medicos/me/servicios"), medico, null, status().isOk()));
        assertThat(catalogo.path("servicios")).hasSize(3);
        assertThat(catalogo.path("sedes")).hasSize(2);

        JsonNode configurados = json(llamar(put("/medicos/me/servicios"), medico, """
                {"servicios":[{"servicioId":%d,"sedeIds":[%d,%d]},{"servicioId":%d,"sedeIds":[%d]}]}
                """.formatted(CONSULTA_CARDIOLOGIA, SEDE_CENTRO_CLINICA, SEDE_NORTE_CLINICA,
                ELECTROCARDIOGRAMA, SEDE_CENTRO_CLINICA), status().isOk()));
        assertThat(configurados).hasSize(3);

        llamar(put("/medicos/me/servicios"), medico, """
                {"servicios":[{"servicioId":%d,"sedeIds":[%d]}]}
                """.formatted(CONSULTA_CARDIOLOGIA, SEDE_DE_OTRA_IPS), status().isUnprocessableEntity());
        llamar(put("/medicos/me/servicios"), medico, """
                {"servicios":[{"servicioId":%d,"sedeIds":[%d]}]}
                """.formatted(SERVICIO_DE_OTRA_ESPECIALIDAD, SEDE_CENTRO_CLINICA), status().isUnprocessableEntity());
        llamar(put("/medicos/me/servicios"), medico, """
                {"servicios":[{"servicioId":%d,"sedeIds":[]}]}
                """.formatted(CONSULTA_CARDIOLOGIA), status().isUnprocessableEntity());

        // Un servicio no habilitado (Prueba de esfuerzo quedó fuera) no se puede publicar.
        llamar(post("/medicos/me/bloques"), medico,
                bloque(8L, SEDE_NORTE_CLINICA, List.of(dias(3)), "08:00", "10:00", 30),
                status().isUnprocessableEntity());

        // --- Cola de espera + publicación con reasignación FIFO automática -------
        llamar(post("/colas-espera/unirse?especialidadId=" + CARDIOLOGIA + "&ipsId=" + CLINICA_CENTRAL),
                carlos, null, status().isCreated());
        JsonNode cola = json(llamar(get("/medicos/me/cola-espera"), medico, null, status().isOk()));
        assertThat(cola).hasSize(1);
        assertThat(cola.get(0).path("paciente").path("nombreCompleto").asText()).isEqualTo("Carlos Alberto Mendoza");

        JsonNode creados = json(llamar(post("/medicos/me/bloques"), medico,
                bloque(CONSULTA_CARDIOLOGIA, SEDE_CENTRO_CLINICA, List.of(dias(3), dias(4)), "08:00", "10:00", 30),
                status().isCreated()));
        assertThat(creados).hasSize(2);
        JsonNode bloque1 = creados.get(0);
        JsonNode bloque2 = creados.get(1);
        assertThat(bloque1.path("cuposTotales").asLong()).isEqualTo(4);
        // El primer cupo publicado se asignó a Carlos, que estaba primero en la cola.
        assertThat(bloque1.path("cuposReservados").asLong()).isEqualTo(1);
        assertThat(bloque1.path("citas").get(0).path("pacienteNombre").asText()).isEqualTo("Carlos Alberto Mendoza");
        assertThat(json(llamar(get("/medicos/me/cola-espera"), medico, null, status().isOk()))).isEmpty();

        // El paciente ve los cupos nuevos con su servicio y sede.
        String publicos = cuerpo(llamar(get("/disponibilidades?especialidadId=" + CARDIOLOGIA
                + "&ipsId=" + CLINICA_CENTRAL), null, null, status().isOk()));
        assertThat(publicos).contains("Consulta de cardiología").contains("Sede Centro");

        // --- Cruce de horarios ---------------------------------------------------
        MvcResult cruce = llamar(post("/medicos/me/bloques"), medico,
                bloque(ELECTROCARDIOGRAMA, SEDE_CENTRO_CLINICA, List.of(dias(3)), "09:00", "11:00", 20),
                status().isConflict());
        assertThat(json(cruce).path("mensaje").asText()).contains("Consulta de cardiología");

        // --- Asignación manual desde la cola -----------------------------------
        JsonNode solicitud = json(llamar(post("/colas-espera/unirse?especialidadId=" + CARDIOLOGIA
                + "&ipsId=" + CLINICA_CENTRAL), andres, null, status().isCreated()));
        long solicitudId = solicitud.path("id").asLong();
        JsonNode compatibles = json(llamar(get("/medicos/me/cola-espera/" + solicitudId + "/cupos-compatibles"),
                medico, null, status().isOk()));
        long cupoDelBloque1 = -1;
        for (JsonNode cupo : compatibles) {
            if (cupo.path("bloqueId").asLong() == bloque1.path("id").asLong()) {
                cupoDelBloque1 = cupo.path("id").asLong();
                break;
            }
        }
        assertThat(cupoDelBloque1).isPositive();

        JsonNode asignada = json(llamar(post("/medicos/me/cola-espera/" + solicitudId
                + "/asignar?disponibilidadId=" + cupoDelBloque1), medico, null, status().isCreated()));
        assertThat(asignada.path("estado").asText()).isEqualTo("PROGRAMADA");

        String citasAndres = cuerpo(llamar(get("/citas/mias"), andres, null, status().isOk()));
        assertThat(citasAndres).contains("Consulta de cardiología").contains("Sede Centro");
        String notificacionesAndres = cuerpo(llamar(get("/notificaciones"), andres, null, status().isOk()));
        assertThat(notificacionesAndres).contains("Tu médico te asignó un cupo");
        JsonNode colasAndres = json(llamar(get("/colas-espera/mias"), andres, null, status().isOk()));
        assertThat(colasAndres.get(0).path("estado").asText()).isEqualTo("ASIGNADO");

        // El mismo cupo ya no se puede volver a asignar.
        llamar(post("/medicos/me/cola-espera/" + solicitudId + "/asignar?disponibilidadId=" + cupoDelBloque1),
                medico, null, status().isUnprocessableEntity());

        // --- Cancelación de bloques -------------------------------------------
        llamar(post("/medicos/me/bloques/" + bloque1.path("id").asLong() + "/cancelar"), medico, null,
                status().isUnprocessableEntity());
        JsonNode cancelado = json(llamar(post("/medicos/me/bloques/" + bloque2.path("id").asLong() + "/cancelar"),
                medico, null, status().isOk()));
        assertThat(cancelado.path("estado").asText()).isEqualTo("CANCELADO");

        // Los cupos del bloque cancelado desaparecen de la búsqueda del paciente.
        JsonNode libres = json(llamar(get("/disponibilidades?especialidadId=" + CARDIOLOGIA
                + "&ipsId=" + CLINICA_CENTRAL + "&fecha=" + dias(4)), null, null, status().isOk()));
        assertThat(libres).isEmpty();

        // Otro médico no puede ver ni tocar ese bloque.
        String otraMedica = token("maria.gomez@medicitas.com", "Medico123*");
        llamar(get("/medicos/me/bloques/" + bloque1.path("id").asLong()), otraMedica, null, status().isForbidden());

        // --- Acciones sobre citas desde la agenda -------------------------------
        JsonNode agenda = json(llamar(get("/citas/agenda"), medico, null, status().isOk()));
        long citaCarlos = -1;
        for (JsonNode cita : agenda) {
            if (cita.path("paciente").path("email").asText().equals("carlos.mendoza@email.com")) {
                citaCarlos = cita.path("id").asLong();
            }
        }
        assertThat(citaCarlos).isPositive();

        // Es una cita futura: aún no se puede marcar como atendida.
        llamar(post("/citas/" + citaCarlos + "/atender"), medico, null, status().isUnprocessableEntity());
        // Un paciente no puede usar el endpoint del médico.
        llamar(post("/citas/" + citaCarlos + "/cancelar-por-medico"), carlos, null, status().isForbidden());

        JsonNode canceladaPorMedico = json(llamar(post("/citas/" + citaCarlos + "/cancelar-por-medico"),
                medico, null, status().isOk()));
        assertThat(canceladaPorMedico.path("estado").asText()).isEqualTo("CANCELADA");
        assertThat(canceladaPorMedico.path("disponibilidad").path("estado").asText()).isEqualTo("CANCELADO");
        assertThat(cuerpo(llamar(get("/notificaciones"), carlos, null, status().isOk())))
                .contains("Tu cita fue cancelada por el profesional");
    }

    @Test
    @DisplayName("la cola respeta el servicio y la sede que pidió el paciente")
    void colaRespetaServicioYSede() throws Exception {
        // Medicina General en IPS Salud Vital: servicio 3 = Retiro de suturas, sede 2 = Sede Norte.
        String medica = token("maria.gomez@medicitas.com", "Medico123*");
        String carlos = token("carlos.mendoza@email.com", "Paciente123*");

        // Un servicio de otra especialidad se rechaza al entrar en la cola.
        llamar(post("/colas-espera/unirse?especialidadId=1&ipsId=1&servicioId=" + CONSULTA_CARDIOLOGIA),
                carlos, null, status().isUnprocessableEntity());

        JsonNode solicitud = json(llamar(post("/colas-espera/unirse?especialidadId=1&ipsId=1&servicioId=3&sedeId=2"),
                carlos, null, status().isCreated()));
        assertThat(solicitud.path("servicio").path("nombre").asText()).isEqualTo("Retiro de suturas");
        assertThat(solicitud.path("sede").path("nombre").asText()).isEqualTo("Sede Norte");

        // Un bloque de otro servicio no se le asigna automáticamente ni cuenta como compatible.
        JsonNode consulta = json(llamar(post("/medicos/me/bloques"), medica,
                bloque(1L, 1L, List.of(dias(6)), "08:00", "09:00", 30), status().isCreated()));
        assertThat(consulta.get(0).path("cuposReservados").asLong()).isZero();
        assertThat(json(llamar(get("/medicos/me/cola-espera/" + solicitud.path("id").asLong() + "/cupos-compatibles"),
                medica, null, status().isOk()))).isEmpty();

        // Un bloque del mismo servicio y sede sí lo atiende la reasignación FIFO.
        JsonNode suturas = json(llamar(post("/medicos/me/bloques"), medica,
                bloque(3L, 2L, List.of(dias(6)), "10:00", "11:00", 30), status().isCreated()));
        assertThat(suturas.get(0).path("cuposReservados").asLong()).isEqualTo(1);
        assertThat(json(llamar(get("/medicos/me/cola-espera"), medica, null, status().isOk()))).isEmpty();
    }

    @Test
    @DisplayName("el médico cambia su contraseña con el endpoint común de perfil")
    void cambioDeClaveDelMedico() throws Exception {
        String token = token("maria.gomez@medicitas.com", "Medico123*");

        llamar(put("/usuarios/me/contrasena"), token, """
                {"actual":"incorrecta","nueva":"NuevaClave123*"}
                """, status().isBadRequest());
        llamar(put("/usuarios/me/contrasena"), token, """
                {"actual":"Medico123*","nueva":"sinmayusculas123"}
                """, status().isBadRequest());
        llamar(put("/usuarios/me/contrasena"), token, """
                {"actual":"Medico123*","nueva":"NuevaClave123*"}
                """, status().isNoContent());

        String nuevoToken = token("maria.gomez@medicitas.com", "NuevaClave123*");
        llamar(post("/auth/login"), null, """
                {"email":"maria.gomez@medicitas.com","password":"Medico123*"}
                """, status().isUnauthorized());

        // Se restaura la contraseña: otros casos de esta clase inician sesión con ella y
        // JUnit no garantiza el orden de ejecución.
        llamar(put("/usuarios/me/contrasena"), nuevoToken, """
                {"actual":"NuevaClave123*","nueva":"Medico123*"}
                """, status().isNoContent());
    }

    private MvcResult llamar(MockHttpServletRequestBuilder peticion, String token, String cuerpo,
                             ResultMatcher esperado) throws Exception {
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        return mockMvc.perform(peticion).andExpect(esperado).andReturn();
    }

    private String token(String correo, String clave) throws Exception {
        return json(llamar(post("/auth/login"), null, """
                {"email":"%s","password":"%s"}
                """.formatted(correo, clave), status().isOk())).path("token").asText();
    }

    private String bloque(long servicioId, long sedeId, List<String> fechas, String inicio, String fin, int duracion)
            throws Exception {
        List<String> comillas = new ArrayList<>();
        fechas.forEach(f -> comillas.add("\"" + f + "\""));
        return """
                {"servicioId":%d,"sedeId":%d,"fechas":[%s],"horaInicio":"%s","horaFin":"%s","duracionCupoMinutos":%d}
                """.formatted(servicioId, sedeId, String.join(",", comillas), inicio, fin, duracion);
    }

    private static String dias(int desdeHoy) {
        return LocalDate.now().plusDays(desdeHoy).toString();
    }

    private String cuerpo(MvcResult resultado) throws Exception {
        return resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private JsonNode json(MvcResult resultado) throws Exception {
        return objectMapper.readTree(cuerpo(resultado));
    }
}
