# MediCitas API: contrato para la app Flutter

## Base
- **URL base:** `http://<host>:8080/api/v1`
  - Emulador Android: `http://10.0.2.2:8080/api/v1`
  - Dispositivo físico: la IP local del PC, por ejemplo `http://192.168.x.x:8080/api/v1`
- **Formato:** JSON, UTF-8.
- **Autenticación:** JWT. Envía el header `Authorization: Bearer <token>`. El token dura 24 horas.
- **Roles:** `ROLE_PACIENTE`, `ROLE_MEDICO`, `ROLE_ADMIN`.
- **Fechas:** `LocalDate` = `"2026-10-17"`, `LocalTime` = `"08:00:00"`, `LocalDateTime` = `"2026-10-16T19:37:08"` (sin zona horaria).
- **Swagger:** `/api/v1/swagger-ui.html`

## Enums
| Enum | Valores |
|---|---|
| TipoDocumento | `CC`, `TI`, `CE`, `PASAPORTE` |
| TipoCita | `CONSULTA`, `EXAMEN` |
| EstadoCita | `PROGRAMADA`, `ATENDIDA`, `CANCELADA`, `APLAZADA` |
| EstadoDisponibilidad | `LIBRE`, `OCUPADO`, `CANCELADO` |
| EstadoColaEspera | `EN_ESPERA`, `ASIGNADO`, `CANCELADO` |
| EstadoAutorizacion | `HABILITADO`, `REVOCADO` |
| TipoNotificacion | `PUSH`, `EMAIL` |

## Modelos de respuesta
```
Usuario        { id, numDocumento, tipoDocumento, nombreCompleto, email, rol, telefono, eps: Eps|null, fechaRegistro }
Eps            { id, nombre, codigoBdua, activa }
Ips            { id, nombre, nit, direccion, ciudad, telefono }
Especialidad   { id, nombre, descripcion }
Medico         { id, usuario: Usuario, especialidad: Especialidad, ips: Ips, registroMedico }
Disponibilidad { id, medico: Medico, fecha, horaInicio, horaFin, estado }
Cita           { id, paciente: Usuario, disponibilidad: Disponibilidad, ips: Ips, tipo, estado, observaciones, fechaCreacion }
ColaEspera     { id, paciente: Usuario, especialidad: Especialidad, ips: Ips, fechaSolicitud, estado }
HistoriaClinica{ id, paciente: Usuario, ipsOrigen: Ips, diagnostico, tratamiento, contenido, fechaRegistro }
Autorizacion   { id, historia: HistoriaClinica, ipsSolicitante: Ips, estado, fechaAutorizacion, fechaRevocacion }
Notificacion   { id, titulo, mensaje, tipo, leida, fechaEnvio }
ResultadoAfiliacion { afiliadoActivo, epsNombre, epsCodigo, epsSoportadaEnRegion, mensajeOrientacion }
Error          { status, error, mensaje, mensajeOrientacion, timestamp }
```

## Endpoints

`*` = campo obligatorio.

### Autenticación (`/auth`)
| Método | Ruta | Acceso | Entrada | Respuesta |
|---|---|---|---|---|
| POST | `/auth/login` | público | `{ numDocumento, password }` o `{ email, password }` | 200 `{ token, tipoToken, usuario: Usuario }` |
| GET | `/auth/validar-bdua?tipoDocumento=CC&numDocumento=...` | público | query | 200 `ResultadoAfiliacion` |
| POST | `/auth/registro-paciente` | público | `{ numDocumento*, tipoDocumento*, nombreCompleto*, email*, password*, telefono }` | 201 `Usuario` |
| POST | `/auth/registro-medico` | ADMIN | `{ numDocumento*, tipoDocumento*, nombreCompleto*, email*, password*, telefono, especialidadId*, ipsId*, registroMedico* }` | 201 `Usuario` |

**Registro de paciente:** la cédula tiene que existir en la tabla de afiliados y pertenecer a una EPS con convenio. Si no, la API responde **400** con `mensajeOrientacion`, que conviene mostrar al usuario. Lo recomendable es llamar primero a `/auth/validar-bdua`.

### Perfil (`/usuarios/me`, cualquier usuario autenticado)
| Método | Ruta | Entrada | Respuesta |
|---|---|---|---|
| GET | `/usuarios/me` | – | `Usuario` |
| PUT | `/usuarios/me` | `{ email*, telefono }` | `Usuario` |
| PUT | `/usuarios/me/contrasena` | `{ actual*, nueva* }` | 204 |

Siempre opera sobre el usuario del token. Nombre y documento no se pueden cambiar. Si la contraseña actual no coincide, si la nueva no cumple los requisitos (mínimo 8 caracteres, una mayúscula y un número) o si el correo ya pertenece a otra cuenta, responde **400**. No responde 401, para que la app no cierre la sesión.

### Catálogos (`/entidades`, todos públicos)
| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/entidades/eps` | `Eps[]` |
| GET | `/entidades/ips` | `Ips[]` |
| GET | `/entidades/ips/por-eps/{epsId}` | `Ips[]` |
| GET | `/entidades/especialidades` | `Especialidad[]` |

### Disponibilidades (`/disponibilidades`)
| Método | Ruta | Acceso | Entrada | Respuesta |
|---|---|---|---|---|
| GET | `/disponibilidades?especialidadId=&ipsId=&fecha=` | público | `fecha` (YYYY-MM-DD) es opcional | `Disponibilidad[]` (horarios libres) |
| POST | `/disponibilidades` | MEDICO | `{ fecha*, horaInicio*, horaFin* }` | 201 `Disponibilidad` |
| GET | `/disponibilidades/mias` | MEDICO | – | `Disponibilidad[]` |

### Citas (`/citas`)
| Método | Ruta | Acceso | Entrada | Respuesta |
|---|---|---|---|---|
| POST | `/citas/agendar` | PACIENTE | `{ disponibilidadId*, tipo (por defecto CONSULTA), observaciones }` | 201 `Cita` |
| POST | `/citas/{id}/cancelar` | PACIENTE | – | 200 `Cita` |
| POST | `/citas/{id}/aplazar?nuevaDisponibilidadId=` | PACIENTE | query | 200 `Cita` |
| GET | `/citas/mias` | PACIENTE | – | `Cita[]` |
| GET | `/citas/agenda` | MEDICO | – | `Cita[]` |

Cuando se cancela una cita, el horario liberado se asigna automáticamente al primero de la cola de espera (orden FIFO).

### Cola de espera (`/colas-espera`, solo PACIENTE)
| Método | Ruta | Entrada | Respuesta |
|---|---|---|---|
| POST | `/colas-espera/unirse?especialidadId=&ipsId=` | query | 201 `ColaEspera` |
| DELETE | `/colas-espera/{id}` | – | 204 |
| GET | `/colas-espera/mias` | – | `ColaEspera[]` |

### Historia clínica (`/historias-clinicas`)
| Método | Ruta | Acceso | Entrada | Respuesta |
|---|---|---|---|---|
| GET | `/historias-clinicas/mias` | PACIENTE | – | `HistoriaClinica[]` |
| GET | `/historias-clinicas/autorizaciones/mias` | PACIENTE | – | `Autorizacion[]` |
| POST | `/historias-clinicas/{historiaId}/autorizar?ipsSolicitanteId=` | PACIENTE | query | `Autorizacion` |
| POST | `/historias-clinicas/{historiaId}/revocar?ipsSolicitanteId=` | PACIENTE | query | `Autorizacion` |
| GET | `/historias-clinicas/paciente/{pacienteId}/desde-mi-ips?ipsSolicitanteId=` | MEDICO/ADMIN | query | `HistoriaClinica[]` (403 si no hay autorización) |
| POST | `/historias-clinicas` | MEDICO | `{ pacienteId*, diagnostico*, tratamiento, contenido* }` | 201 `HistoriaClinica` |

### Notificaciones (`/notificaciones`, cualquier usuario autenticado)
| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/notificaciones` | `Notificacion[]` |
| PUT | `/notificaciones/{id}/marcar-leida` | 204 |

## Códigos de error
Todos los errores devuelven el modelo `Error`. Muestra `mensaje` al usuario, y también `mensajeOrientacion` cuando venga.

| Código | Cuándo |
|---|---|
| 400 | Validación, parámetros inválidos o afiliación EPS bloqueada |
| 401 | Sin token, token vencido o credenciales inválidas (hay que volver al login) |
| 403 | El rol no tiene permiso, o no hay autorización para ver la historia clínica |
| 404 | Recurso no encontrado |
| 409 | Conflicto, por ejemplo un horario que otra persona tomó al mismo tiempo, o datos duplicados |
| 422 | Regla de negocio, por ejemplo la hora de fin antes que la de inicio |

## Datos de prueba

### Usuarios
| Usuario | Contraseña | Rol |
|---|---|---|
| carlos.mendoza@email.com | `Paciente123*` | paciente |
| andres.torres@email.com | `Paciente123*` | paciente |
| luz.marin@email.com | `Paciente123*` | paciente |
| maria.gomez@medicitas.com | `Medico123*` | médico (Medicina General, IPS Salud Vital) |
| jorge.rivera@medicitas.com | `Medico123*` | médico (Cardiología, Clínica Central) |
| admin@medicitas.com | `Admin123*` | admin |

### Cédulas para `validar-bdua`
| Cédula | Resultado |
|---|---|
| `1067444555` | afiliada y válida para registrarse (no tiene usuario todavía) |
| `1067890124` | afiliada y válida para registrarse (no tiene usuario todavía) |
| `1067890128` | afiliada, pero su EPS no tiene convenio: se rechaza |
| cualquier otra | sin afiliación |

### IDs de catálogo
- **IPS:** 1 = Salud Vital, 2 = Clínica Central, 3 = Sinú Norte
- **Especialidades:** 1 = Medicina General, 2 = Pediatría, 3 = Cardiología, 4 = Ginecología, 5 = Odontología

### Ruta no disponible
`/incidencias/categorias` existe pero todavía no está implementada.
