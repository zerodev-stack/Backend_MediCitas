package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.AutenticacionUseCase;
import co.edu.monteria.medicitas.application.ports.out.EpsRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.EspecialidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.IpsRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.JwtPort;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.UsuarioRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.VerificacionAfiliacionPort;
import co.edu.monteria.medicitas.domain.exception.AfiliacionNoValidaException;
import co.edu.monteria.medicitas.domain.exception.CredencialesInvalidasException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.domain.model.Eps;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.ResultadoAfiliacion;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.RolUsuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio que orquesta el registro y la autenticación de usuarios.
 *
 * POR QUÉ Y CÓMO DE LA VALIDACIÓN BDUA/EPS:
 * En Colombia, el estado de afiliación a la EPS es obligatorio para acceder a la red de
 * servicios de salud. Durante el registro de un paciente, este servicio invoca el puerto
 * 'VerificacionAfiliacionPort', respaldado por un adaptador simulado de la BDUA.
 *
 * Si la EPS del paciente no está en la red activa de la región de Montería (por ejemplo,
 * una EPS sin convenio local vigente), el registro se bloquea preventivamente con una
 * 'AfiliacionNoValidaException' que incluye una guía de orientación para el trámite de
 * traslado o movilidad de EPS.
 */
@Service
@Transactional
public class AuthService implements AutenticacionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final EpsRepositoryPort epsRepository;
    private final IpsRepositoryPort ipsRepository;
    private final EspecialidadRepositoryPort especialidadRepository;
    private final MedicoRepositoryPort medicoRepository;
    private final VerificacionAfiliacionPort verificacionAfiliacionPort;
    private final JwtPort jwtPort;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepositoryPort usuarioRepository,
                       EpsRepositoryPort epsRepository,
                       IpsRepositoryPort ipsRepository,
                       EspecialidadRepositoryPort especialidadRepository,
                       MedicoRepositoryPort medicoRepository,
                       VerificacionAfiliacionPort verificacionAfiliacionPort,
                       JwtPort jwtPort,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.epsRepository = epsRepository;
        this.ipsRepository = ipsRepository;
        this.especialidadRepository = especialidadRepository;
        this.medicoRepository = medicoRepository;
        this.verificacionAfiliacionPort = verificacionAfiliacionPort;
        this.jwtPort = jwtPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario autenticar(String email, String password) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            // Mismo mensaje que el correo inexistente: no se revela qué cuentas existen.
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos.");
        }

        return usuario;
    }

    @Override
    public String generarToken(Usuario usuario) {
        return jwtPort.generarToken(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoAfiliacion validarAfiliacionBdua(TipoDocumento tipoDocumento, String numDocumento) {
        return verificacionAfiliacionPort.consultarAfiliacionBdua(tipoDocumento, numDocumento);
    }

    @Override
    public Usuario registrarPaciente(String numDocumento, TipoDocumento tipoDocumento, String nombreCompleto,
                                     String email, String password, String telefono) {
        verificarDuplicados(numDocumento, email);

        ResultadoAfiliacion res = verificacionAfiliacionPort.consultarAfiliacionBdua(tipoDocumento, numDocumento);
        if (!res.isAfiliadoActivo() || !res.isEpsSoportadaEnRegion()) {
            throw new AfiliacionNoValidaException(
                    "Su afiliación en la BDUA (" + res.getEpsNombre() + ") no se encuentra activa en la red de convenio de Montería.",
                    res.getMensajeOrientacion()
            );
        }

        Eps eps = epsRepository.buscarPorCodigoBdua(res.getEpsCodigo())
                .orElseGet(() -> epsRepository.obtenerTodas().stream().findFirst()
                        .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una EPS disponible en la región")));

        Usuario paciente = new Usuario();
        paciente.setNumDocumento(numDocumento);
        paciente.setTipoDocumento(tipoDocumento);
        paciente.setNombreCompleto(nombreCompleto);
        paciente.setEmail(email);
        paciente.setPasswordHash(passwordEncoder.encode(password));
        paciente.setRol(RolUsuario.ROLE_PACIENTE);
        paciente.setEps(eps);
        paciente.setTelefono(telefono);
        paciente.setFechaRegistro(LocalDateTime.now());

        return usuarioRepository.guardar(paciente);
    }

    @Override
    public Usuario registrarMedico(String numDocumento, TipoDocumento tipoDocumento, String nombreCompleto,
                                   String email, String password, String telefono, Long especialidadId,
                                   Long ipsId, String registroMedico) {
        verificarDuplicados(numDocumento, email);

        Especialidad especialidad = especialidadRepository.buscarPorId(especialidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));

        Ips ips = ipsRepository.buscarPorId(ipsId)
                .orElseThrow(() -> new RecursoNoEncontradoException("IPS no encontrada"));

        Usuario usuarioMedico = new Usuario();
        usuarioMedico.setNumDocumento(numDocumento);
        usuarioMedico.setTipoDocumento(tipoDocumento);
        usuarioMedico.setNombreCompleto(nombreCompleto);
        usuarioMedico.setEmail(email);
        usuarioMedico.setPasswordHash(passwordEncoder.encode(password));
        usuarioMedico.setRol(RolUsuario.ROLE_MEDICO);
        usuarioMedico.setTelefono(telefono);
        usuarioMedico.setFechaRegistro(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepository.guardar(usuarioMedico);

        Medico medico = new Medico();
        medico.setUsuario(usuarioGuardado);
        medico.setEspecialidad(especialidad);
        medico.setIps(ips);
        medico.setRegistroMedico(registroMedico);
        medicoRepository.guardar(medico);

        return usuarioGuardado;
    }

    private void verificarDuplicados(String numDocumento, String email) {
        if (usuarioRepository.existePorEmail(email)) {
            throw new ReglaNegocioException("El correo electrónico ya se encuentra registrado.");
        }
        if (usuarioRepository.existePorNumDocumento(numDocumento)) {
            throw new ReglaNegocioException("El número de documento ya se encuentra registrado.");
        }
    }
}
