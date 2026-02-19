package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.ContaBancariaDTO;
import br.edu.ifpb.ads.acvd.service.ContaBancariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users/me/conta-bancaria")
@RequiredArgsConstructor
public class ContaBancariaController {

    private final ContaBancariaService contaBancariaService;

    @GetMapping
    public ResponseEntity<ContaBancariaDTO> obter(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(contaBancariaService.obter(userId));
    }

    @PutMapping
    public ResponseEntity<ContaBancariaDTO> salvar(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestBody ContaBancariaDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(contaBancariaService.atualizarConta(userId, dto));
    }
}