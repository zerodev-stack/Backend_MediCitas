-- =============================================================================
-- ESQUEMA Y DATOS INICIALES DE MEDICITAS (Montería, Córdoba, Colombia)
-- =============================================================================
-- Este archivo es la ÚNICA fuente de verdad del esquema. Spring Boot lo ejecuta en
-- cada arranque antes de que Hibernate lo valide (spring.jpa.defer-datasource-initialization),
-- y todas las sentencias son idempotentes, de modo que puede aplicarse sobre una base
-- existente sin duplicar nada.
--
-- Cumplimiento del marco legal colombiano: Res. 1995 de 1999 y Res. 3280 de 2018
-- sobre manejo de historia clínica y autorización de acceso entre IPS.

-- ---------------------------------------------------------------------------
-- 1. EPS (Entidades Promotoras de Salud)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eps (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    codigo_bdua VARCHAR(20) NOT NULL UNIQUE,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------------
-- 2. IPS (Instituciones Prestadoras de Servicios de Salud)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ips (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(200) NOT NULL,
    ciudad VARCHAR(80) NOT NULL DEFAULT 'Montería',
    telefono VARCHAR(30)
);

-- ---------------------------------------------------------------------------
-- 3. RED DE AFILIACIÓN EPS - IPS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eps_ips (
    id BIGSERIAL PRIMARY KEY,
    eps_id BIGINT NOT NULL REFERENCES eps(id) ON DELETE CASCADE,
    ips_id BIGINT NOT NULL REFERENCES ips(id) ON DELETE CASCADE,
    convenio_activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_eps_ips UNIQUE (eps_id, ips_id)
);

-- ---------------------------------------------------------------------------
-- 4. USUARIOS (pacientes, médicos y administrador)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    num_documento VARCHAR(30) NOT NULL UNIQUE,
    tipo_documento VARCHAR(10) NOT NULL DEFAULT 'CC', -- CC, TI, CE, PASAPORTE
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL, -- ROLE_PACIENTE, ROLE_MEDICO, ROLE_ADMIN
    eps_id BIGINT REFERENCES eps(id),
    telefono VARCHAR(30),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- 5. ESPECIALIDADES MÉDICAS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS especialidades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

-- ---------------------------------------------------------------------------
-- 6. MÉDICOS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    especialidad_id BIGINT NOT NULL REFERENCES especialidades(id),
    ips_id BIGINT NOT NULL REFERENCES ips(id),
    registro_medico VARCHAR(50) NOT NULL UNIQUE -- Registro ReTHUS
);

-- ---------------------------------------------------------------------------
-- 7. DISPONIBILIDAD MÉDICA
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS disponibilidades_medicas (
    id BIGSERIAL PRIMARY KEY,
    medico_id BIGINT NOT NULL REFERENCES medicos(id) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'LIBRE', -- LIBRE, OCUPADO, CANCELADO
    CONSTRAINT ck_horario_orden CHECK (hora_fin > hora_inicio)
);

-- ---------------------------------------------------------------------------
-- 8. CITAS MÉDICAS
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS citas (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES usuarios(id),
    -- Una disponibilidad no puede generar dos citas: es la garantía de fondo contra
    -- el doble agendamiento cuando dos peticiones compiten por el mismo cupo.
    disponibilidad_id BIGINT NOT NULL UNIQUE REFERENCES disponibilidades_medicas(id),
    ips_id BIGINT NOT NULL REFERENCES ips(id),
    tipo VARCHAR(20) NOT NULL DEFAULT 'CONSULTA', -- CONSULTA, EXAMEN
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA', -- PROGRAMADA, ATENDIDA, CANCELADA, APLAZADA
    observaciones TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- 9. COLA DE ESPERA FIFO
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS colas_espera (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES usuarios(id),
    especialidad_id BIGINT NOT NULL REFERENCES especialidades(id),
    ips_id BIGINT NOT NULL REFERENCES ips(id),
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'EN_ESPERA' -- EN_ESPERA, ASIGNADO, CANCELADO
);

-- Índice que sostiene la consulta de cabecera FIFO
-- (WHERE especialidad_id, ips_id, estado ORDER BY fecha_solicitud ASC).
CREATE INDEX IF NOT EXISTS ix_colas_espera_fifo
    ON colas_espera (especialidad_id, ips_id, estado, fecha_solicitud);

-- ---------------------------------------------------------------------------
-- 10. HISTORIA CLÍNICA (custodia por IPS de origen)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historias_clinicas (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES usuarios(id),
    ips_origen_id BIGINT NOT NULL REFERENCES ips(id),
    diagnostico VARCHAR(255) NOT NULL,
    tratamiento TEXT,
    contenido TEXT NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- 11. AUTORIZACIÓN DE ACCESO ENTRE IPS (Res. 1995/1999)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS autorizaciones_acceso (
    id BIGSERIAL PRIMARY KEY,
    historia_id BIGINT NOT NULL REFERENCES historias_clinicas(id) ON DELETE CASCADE,
    ips_solicitante_id BIGINT NOT NULL REFERENCES ips(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'HABILITADO', -- HABILITADO, REVOCADO
    fecha_autorizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_revocacion TIMESTAMP,
    CONSTRAINT uk_historia_ips_solicitante UNIQUE (historia_id, ips_solicitante_id)
);

-- ---------------------------------------------------------------------------
-- 12. NOTIFICACIONES
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'PUSH', -- PUSH, EMAIL
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- 13. AFILIADOS EPS
-- ---------------------------------------------------------------------------
-- Personas afiliadas a una EPS, identificadas por su número de cédula. Es la fuente
-- que consulta la verificación de afiliación (BduaAdapter).
CREATE TABLE IF NOT EXISTS eps_affiliates (
    id BIGSERIAL PRIMARY KEY,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(60) NOT NULL,
    middle_name VARCHAR(60),
    first_last_name VARCHAR(60) NOT NULL,
    second_last_name VARCHAR(60),
    eps_id BIGINT NOT NULL REFERENCES eps(id)
);

-- ============================================================================
-- DATOS INICIALES
-- ============================================================================

INSERT INTO eps (id, nombre, codigo_bdua, activa) VALUES
(1, 'EPS Mutualser', 'EPS008', true),
(2, 'EPS Coosalud', 'EPS002', true),
(3, 'EPS Sanitas (sin convenio activo en la región)', 'EPS005', false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ips (id, nombre, nit, direccion, ciudad, telefono) VALUES
(1, 'IPS Salud Vital Montería - Barrio La Julia', '900123456-1', 'Calle 29 # 14-20, La Julia', 'Montería', '6047891000'),
(2, 'Clínica Montería Central - Centro', '900987654-2', 'Carrera 3 # 24-45, Centro', 'Montería', '6047892000'),
(3, 'Centro Médico Sinú Norte - Mocarí', '900456789-3', 'Avenida Circunvalar # 48-12', 'Montería', '6047893000')
ON CONFLICT (id) DO NOTHING;

INSERT INTO eps_ips (eps_id, ips_id, convenio_activo) VALUES
(1, 1, true), (1, 2, true), (1, 3, true),
(2, 1, true), (2, 2, true)
ON CONFLICT (eps_id, ips_id) DO NOTHING;

INSERT INTO especialidades (id, nombre, descripcion) VALUES
(1, 'Medicina General', 'Consulta médica primaria y valoración integral'),
(2, 'Pediatría', 'Atención médica especializada para bebés, niños y adolescentes'),
(3, 'Cardiología', 'Diagnóstico y tratamiento de enfermedades del corazón'),
(4, 'Ginecología', 'Salud del sistema reproductor femenino y control prenatal'),
(5, 'Odontología', 'Salud oral y tratamientos dentales')
ON CONFLICT (id) DO NOTHING;

-- Usuarios iniciales. Los hash son BCrypt REALES: sin ellos, ningún usuario podría
-- iniciar sesión, porque un hash inventado no verifica contra ninguna contraseña.
--
--   carlos.mendoza@email.com   -> Paciente123*
--   andres.torres@email.com    -> Paciente123*
--   luz.marin@email.com        -> Paciente123*
--   maria.gomez@medicitas.com  -> Medico123*
--   jorge.rivera@medicitas.com -> Medico123*
--   admin@medicitas.com        -> Admin123*
--
-- Los tres pacientes existen para poder demostrar la cola FIFO desde la app:
-- cada uno puede iniciar sesión e inscritos en la misma cola, y se comprueba que
-- la asignación automática respeta el orden de llegada.
INSERT INTO usuarios (id, num_documento, tipo_documento, nombre_completo, email, password_hash, rol, eps_id, telefono) VALUES
(1, '1067890123', 'CC', 'Carlos Alberto Mendoza', 'carlos.mendoza@email.com',
 '$2a$10$6yHRExeItfJGeJsFOsWr1.DJcTED3n/RCo9BFVvgyVnimNmS0U.nq', 'ROLE_PACIENTE', 1, '3001234567'),
(2, '1067999888', 'CC', 'Dra. María Paula Gómez', 'maria.gomez@medicitas.com',
 '$2a$10$FqrnIEOpHrH2jxPRcS6uC.LI6p/hXwBgi0UI8ruYcFq00ZQkGKcRK', 'ROLE_MEDICO', NULL, '3109876543'),
(3, '1067555444', 'CC', 'Dr. Jorge Luis Rivera', 'jorge.rivera@medicitas.com',
 '$2a$10$FqrnIEOpHrH2jxPRcS6uC.LI6p/hXwBgi0UI8ruYcFq00ZQkGKcRK', 'ROLE_MEDICO', NULL, '3157778899'),
(4, '1090000001', 'CC', 'Administración MediCitas', 'admin@medicitas.com',
 '$2a$10$wlmCundx8gVtmbDUd9Wno.hdN.3MSgZlUT0J.OMWG0ywyt9eCO9lm', 'ROLE_ADMIN', NULL, '3000000000'),
(5, '1067111222', 'CC', 'Andrés Felipe Torres', 'andres.torres@email.com',
 '$2a$10$6yHRExeItfJGeJsFOsWr1.DJcTED3n/RCo9BFVvgyVnimNmS0U.nq', 'ROLE_PACIENTE', 1, '3012223344'),
(6, '1067333444', 'CC', 'Luz Marcela Marín', 'luz.marin@email.com',
 '$2a$10$6yHRExeItfJGeJsFOsWr1.DJcTED3n/RCo9BFVvgyVnimNmS0U.nq', 'ROLE_PACIENTE', 2, '3025556677')
ON CONFLICT (id) DO NOTHING;

INSERT INTO medicos (id, usuario_id, especialidad_id, ips_id, registro_medico) VALUES
(1, 2, 1, 1, 'RM-99482-COR'), -- Medicina General en IPS Salud Vital
(2, 3, 3, 2, 'RM-88319-COR')  -- Cardiología en Clínica Montería Central
ON CONFLICT (id) DO NOTHING;

INSERT INTO disponibilidades_medicas (id, medico_id, fecha, hora_inicio, hora_fin, estado) VALUES
(1, 1, CURRENT_DATE + INTERVAL '1 day', '08:00:00', '08:30:00', 'LIBRE'),
(2, 1, CURRENT_DATE + INTERVAL '1 day', '08:30:00', '09:00:00', 'LIBRE'),
(3, 1, CURRENT_DATE + INTERVAL '1 day', '09:00:00', '09:30:00', 'LIBRE'),
(4, 2, CURRENT_DATE + INTERVAL '2 days', '10:00:00', '10:45:00', 'LIBRE'),
(5, 2, CURRENT_DATE + INTERVAL '2 days', '11:00:00', '11:45:00', 'LIBRE')
ON CONFLICT (id) DO NOTHING;

-- Historia clínica de prueba, generada bajo custodia de IPS Salud Vital (id 1).
INSERT INTO historias_clinicas (id, paciente_id, ips_origen_id, diagnostico, tratamiento, contenido) VALUES
(1, 1, 1, 'Hipertensión arterial primaria', 'Losartán 50 mg cada 12 horas y dieta baja en sodio',
 'Control de rutina en IPS Salud Vital. Cifras tensionales 135/85 mmHg. Se ajusta dosis.')
ON CONFLICT (id) DO NOTHING;

-- El paciente autoriza a Clínica Montería Central (id 2) a consultar ese registro:
-- sirve para demostrar el consentimiento y su revocación desde la app.
INSERT INTO autorizaciones_acceso (id, historia_id, ips_solicitante_id, estado) VALUES
(1, 1, 2, 'HABILITADO')
ON CONFLICT (id) DO NOTHING;

-- Afiliados de prueba. Quien no aparece aquí no tiene afiliación activa; el afiliado de
-- EPS Sanitas (id 3, sin convenio en la región) sirve para demostrar el rechazo.
INSERT INTO eps_affiliates (id, document_number, first_name, middle_name, first_last_name, second_last_name, eps_id) VALUES
(1, '1067890123', 'Carlos', 'Alberto', 'Mendoza', NULL, 1),
(2, '1067111222', 'Andrés', 'Felipe', 'Torres', NULL, 1),
(3, '1067333444', 'Luz', 'Marcela', 'Marín', NULL, 2),
(4, '1067890124', 'Ana', 'Lucía', 'Pérez', 'Díaz', 1),
(5, '1067444555', 'Sofía', 'Valentina', 'Herrera', 'Castro', 2),
(6, '1067890128', 'Pedro', 'José', 'Ramírez', 'Soto', 3)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- REALINEACIÓN DE LAS SECUENCIAS
-- ---------------------------------------------------------------------------
-- NECESARIO y no opcional: la semilla anterior inserta identificadores explícitos y
-- PostgreSQL NO adelanta la secuencia BIGSERIAL por esa vía. Sin estas sentencias, el
-- primer usuario, cita o disponibilidad que la aplicación intente crear al vuelo
-- recibiría el identificador 1 y chocaría contra la clave primaria de la fila sembrada.
--
-- Se usa GREATEST(MAX(id), 1) para que la sentencia también sea correcta cuando la
-- tabla está vacía.
SELECT setval(pg_get_serial_sequence('eps', 'id'), GREATEST((SELECT MAX(id) FROM eps), 1));
SELECT setval(pg_get_serial_sequence('ips', 'id'), GREATEST((SELECT MAX(id) FROM ips), 1));
SELECT setval(pg_get_serial_sequence('eps_ips', 'id'), GREATEST((SELECT MAX(id) FROM eps_ips), 1));
SELECT setval(pg_get_serial_sequence('usuarios', 'id'), GREATEST((SELECT MAX(id) FROM usuarios), 1));
SELECT setval(pg_get_serial_sequence('especialidades', 'id'), GREATEST((SELECT MAX(id) FROM especialidades), 1));
SELECT setval(pg_get_serial_sequence('medicos', 'id'), GREATEST((SELECT MAX(id) FROM medicos), 1));
SELECT setval(pg_get_serial_sequence('disponibilidades_medicas', 'id'), GREATEST((SELECT MAX(id) FROM disponibilidades_medicas), 1));
SELECT setval(pg_get_serial_sequence('citas', 'id'), GREATEST((SELECT MAX(id) FROM citas), 1));
SELECT setval(pg_get_serial_sequence('colas_espera', 'id'), GREATEST((SELECT MAX(id) FROM colas_espera), 1));
SELECT setval(pg_get_serial_sequence('historias_clinicas', 'id'), GREATEST((SELECT MAX(id) FROM historias_clinicas), 1));
SELECT setval(pg_get_serial_sequence('autorizaciones_acceso', 'id'), GREATEST((SELECT MAX(id) FROM autorizaciones_acceso), 1));
SELECT setval(pg_get_serial_sequence('notificaciones', 'id'), GREATEST((SELECT MAX(id) FROM notificaciones), 1));
SELECT setval(pg_get_serial_sequence('eps_affiliates', 'id'), GREATEST((SELECT MAX(id) FROM eps_affiliates), 1));
