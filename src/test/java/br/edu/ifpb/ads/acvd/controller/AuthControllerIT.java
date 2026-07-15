package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.RegisterDTO;
import br.edu.ifpb.ads.acvd.entity.Role;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @WithMockUser
    public void TI01_deveBloquearAcessoComEmailNaoInstitucional() throws Exception {
        RegisterDTO dtoInvalido = new RegisterDTO(
                "usuario.comum@gmail.com", "202312345", "8399999999",
                "00011122233", "123456789", new Date(), "ADS", "P4"
        );

        when(userRepository.findByEmail(dtoInvalido.email()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "E-mail não institucional não permitido."));

        mockMvc.perform(post("/auth/complete-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void TI02_deveRegistrarUsuarioDiscenteEEmitirTokenValido() throws Exception {
        RegisterDTO dtoValido = new RegisterDTO(
                "aluno@academico.ifpb.edu.br", "202312345", "8399999999",
                "00011122233", "123456789", new Date(), "ADS", "P4"
        );

        User usuarioSimulado = new User();
        usuarioSimulado.setUserId(UUID.randomUUID());
        usuarioSimulado.setEmail("aluno@academico.ifpb.edu.br");
        usuarioSimulado.setRole(Role.DISCENTE);

        when(userRepository.findByEmail(dtoValido.email())).thenReturn(Optional.of(usuarioSimulado));
        when(tokenService.generateToken(any(User.class))).thenReturn("eyJhbGciOiJub25lIn0.TOKEN_JWT_VALIDO");

        mockMvc.perform(post("/auth/complete-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJub25lIn0.TOKEN_JWT_VALIDO"));
    }

    @Test
    @WithMockUser
    public void TI01B_deveRetornar400AoTentarCadastrarComCamposNulosOuVazios() throws Exception {
        // Simula o envio de um RegisterDTO com campos nulos
        RegisterDTO dtoNulo = new RegisterDTO(
                null, null, null, null, null, null, null, null
        );

        when(userRepository.findByEmail(null))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obrigatórios ausentes ou inválidos."));

        mockMvc.perform(post("/auth/complete-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoNulo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void TI01C_deveRetornar409AoTentarCadastrarComEmailJaExistente() throws Exception {
        RegisterDTO dtoExistente = new RegisterDTO(
                "aluno.existente@academico.ifpb.edu.br", "202312345", "8399999999",
                "00011122233", "123456789", new Date(), "ADS", "P4"
        );

        when(userRepository.findByEmail(dtoExistente.email()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado no sistema."));

        mockMvc.perform(post("/auth/complete-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoExistente)))
                .andExpect(status().isConflict());
    }
}