package co.edu.monteria.medicitas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprueba que el esquema DDL y las entidades JPA no han divergido.
 *
 * En tiempo de ejecución el proyecto usa 'ddl-auto: none' y el esquema vive solo en
 * schema.sql / schema-h2.sql. Esa decisión evita que Hibernate altere la base de datos y
 * elimina la fragilidad de depender del orden entre el script SQL y el
 * EntityManagerFactory, pero deja como responsabilidad de esta prueba detectar que el
 * script y las entidades digan lo mismo.
 *
 * Si alguien añade una columna a una entidad y olvida el script SQL, esta prueba falla al
 * arrancar con 'validate', que es exactamente el objetivo.
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@DisplayName("Esquema DDL consistente con las entidades JPA")
class EsquemaConsistenteConEntidadesTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("todas las tablas del modelo existen con las columnas esperadas")
    void esquemaContieneTodasLasTablasDelModelo() {
        List<String> tablas = jdbcTemplate.queryForList(
                "SELECT LOWER(table_name) FROM information_schema.tables "
                        + "WHERE table_schema = SCHEMA() OR table_schema = 'PUBLIC'",
                String.class);

        assertThat(tablas).contains(
                "eps", "ips", "eps_ips", "usuarios", "especialidades", "medicos",
                "disponibilidades_medicas", "citas", "colas_espera", "historias_clinicas",
                "autorizaciones_acceso", "notificaciones");
    }

    @Test
    @DisplayName("una disponibilidad no puede generar dos citas")
    void disponibilidadNoPuedeTenerDosCitas() {
        Long pacienteId = jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios ORDER BY id LIMIT 1", Long.class);
        Long disponibilidadId = jdbcTemplate.queryForObject(
                "SELECT id FROM disponibilidades_medicas ORDER BY id LIMIT 1", Long.class);
        Long ipsId = jdbcTemplate.queryForObject("SELECT id FROM ips ORDER BY id LIMIT 1", Long.class);

        jdbcTemplate.update("""
                INSERT INTO citas (paciente_id, disponibilidad_id, ips_id, tipo, estado, fecha_creacion)
                VALUES (?, ?, ?, 'CONSULTA', 'PROGRAMADA', CURRENT_TIMESTAMP)
                """, pacienteId, disponibilidadId, ipsId);

        // La restricción debe impedir una segunda cita para el mismo cupo: es la
        // garantía de fondo contra el doble agendamiento.
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO citas (paciente_id, disponibilidad_id, ips_id, tipo, estado, fecha_creacion)
                VALUES (?, ?, ?, 'CONSULTA', 'PROGRAMADA', CURRENT_TIMESTAMP)
                """, pacienteId, disponibilidadId, ipsId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("el DataSource está operativo y los datos iniciales se cargaron")
    void datosInicialesCargados() throws Exception {
        try (var conexion = dataSource.getConnection()) {
            assertThat(conexion.isValid(3)).isTrue();
        }

        Integer usuarios = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
        Integer medicos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM medicos", Integer.class);

        assertThat(usuarios).isGreaterThanOrEqualTo(3);
        assertThat(medicos).isGreaterThanOrEqualTo(2);
    }
}
