package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.RequisicaoCreateDTO;
import br.edu.ifpb.ads.acvd.dto.RequisicaoDTO;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
import br.edu.ifpb.ads.acvd.service.RequisicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/requisicoes")
@RequiredArgsConstructor
public class RequisicaoController {

    private final RequisicaoService requisicaoService;

    @PostMapping
    public ResponseEntity<RequisicaoDTO> criarRequisicao(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RequisicaoCreateDTO dto) throws RegraDeNegocioException {
        UUID discenteId = UUID.fromString(jwt.getSubject());
        RequisicaoDTO response = requisicaoService.criarRequisicao(discenteId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<RequisicaoDTO>> listarMinhasRequisicoes(@AuthenticationPrincipal Jwt jwt) {
        UUID discenteId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(requisicaoService.listarMinhasRequisicoes(discenteId));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<RequisicaoDTO> confirmarRequisicao(@PathVariable UUID id) {
        return ResponseEntity.ok(requisicaoService.confirmarRequisicao(id));
    }
}