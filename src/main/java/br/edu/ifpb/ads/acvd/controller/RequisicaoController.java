package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.RequisicaoDTO;
import br.edu.ifpb.ads.acvd.dto.RequisicaoDetalhesDTO;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
import br.edu.ifpb.ads.acvd.service.RequisicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    // ENDPOINTS DO SERVIDOR

    @PostMapping("/viagens/{viagemId}/adicionar-discente")
    public ResponseEntity<RequisicaoDTO.Response> adicionarDiscente(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID viagemId, @Valid @RequestBody RequisicaoDTO.AdicionarDiscente dto) throws RegraDeNegocioException {
        UUID servidorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(requisicaoService.adicionarDiscenteAViagem(servidorId, viagemId, dto));
    }

    @PostMapping("/viagens/{viagemId}/adicionar-discente/email")
    public ResponseEntity<RequisicaoDTO.Response> adicionarDiscentePorEmail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID viagemId, @Valid @RequestBody RequisicaoDTO.AdicionarDiscentePorEmail dto) throws RegraDeNegocioException {
        UUID servidorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(requisicaoService.adicionarDiscenteAViagemPorEmail(servidorId, viagemId, dto));
    }

    @PatchMapping("/{requisicaoId}/avaliar")
    public ResponseEntity<RequisicaoDTO.Response> avaliarRequisicao(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID requisicaoId, @Valid @RequestBody RequisicaoDTO.Avaliar dto) throws RegraDeNegocioException {
        UUID servidorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(requisicaoService.avaliarRequisicao(servidorId, requisicaoId, dto));
    }

    @GetMapping("/viagens/{viagemId}")
    public ResponseEntity<List<RequisicaoDTO.Response>> listarPorViagem(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID viagemId) {
        UUID servidorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(requisicaoService.listarRequisicoesDaViagem(servidorId, viagemId));
    }

    @GetMapping("/{requisicaoId}/detalhes")
    public ResponseEntity<RequisicaoDetalhesDTO> verDetalhesParaAvaliacao(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID requisicaoId) throws RegraDeNegocioException {
        UUID servidorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(requisicaoService.obterDetalhesParaAvaliacao(servidorId, requisicaoId));
    }

    // ENDPOINTS DO DISCENTE

    @GetMapping("/minhas")
    public ResponseEntity<List<RequisicaoDTO.Response>> listarMinhasRequisicoes(@AuthenticationPrincipal Jwt jwt) {
        UUID discenteId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(requisicaoService.listarMinhasRequisicoes(discenteId));
    }

    @PatchMapping("/{requisicaoId}/enviar")
    public ResponseEntity<RequisicaoDTO.Response> enviarRequisicao(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID requisicaoId) throws RegraDeNegocioException {
        UUID discenteId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(requisicaoService.enviarParaAnalise(discenteId, requisicaoId));
    }
}