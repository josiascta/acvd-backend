package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.LoginResponse;
import br.edu.ifpb.ads.acvd.dto.RegisterDTO;
import br.edu.ifpb.ads.acvd.service.TokenService;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @PostMapping("/complete-register")
    public ResponseEntity<LoginResponse> completeRegister(@RequestBody RegisterDTO dto) {

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado via Google"));

        user.setMatricula(dto.matricula());
        user.setTelefone(dto.telefone());
        user.setNumeroCpf(dto.numeroCpf());
        user.setNumeroRg(dto.numeroRg());
        user.setDataNascimento(dto.dataNascimento());
        user.setCurso(dto.curso());
        user.setTurmaPeriodo(dto.turmaPeriodo());

        userRepository.save(user);

        String token = tokenService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
