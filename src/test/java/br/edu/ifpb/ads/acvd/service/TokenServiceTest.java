package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.Role;
import br.edu.ifpb.ads.acvd.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private TokenService tokenService;

    @Test
    public void deveGerarTokenComClaimCompleteFalseQuandoMatriculaNula() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("aluno@academico.ifpb.edu.br");
        user.setRole(Role.DISCENTE);
        user.setMatricula(null);

        Jwt mockJwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "teste")
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        tokenService.generateToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        Boolean isComplete = captor.getValue().getClaims().getClaim("complete");
        assertFalse(isComplete, "O perfil deve ser considerado incompleto sem matrícula.");
    }

    @Test
    public void deveGerarTokenComClaimCompleteTrueQuandoMatriculaPreenchida() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("aluno@academico.ifpb.edu.br");
        user.setRole(Role.DISCENTE);
        user.setMatricula("202312345");

        Jwt mockJwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "teste")
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        tokenService.generateToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        Boolean isComplete = captor.getValue().getClaims().getClaim("complete");
        assertTrue(isComplete, "O perfil deve ser considerado completo com matrícula.");
    }
}