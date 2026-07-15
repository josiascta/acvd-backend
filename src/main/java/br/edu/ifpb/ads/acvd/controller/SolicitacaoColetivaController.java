package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoColetivaDTO;
import br.edu.ifpb.ads.acvd.service.SolicitacaoColetivaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes-coletivas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SERVIDOR')")
public class SolicitacaoColetivaController {

    private final SolicitacaoColetivaService service;

    @PostMapping("/gerar-e-salvar")
    public ResponseEntity<SolicitacaoColetivaDTO> gerarESalvar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SolicitacaoColetivaDTO dados) {

        UUID userId = UUID.fromString(jwt.getSubject());
        SolicitacaoColetivaDTO salvo = service.processarSolicitacao(userId, dados);

        return ResponseEntity.ok(salvo);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocumento(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = UUID.fromString(jwt.getSubject());

        byte[] pdfBytes = service.gerarPdfSobDemanda(id, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"solicitacao_coletiva_" + id + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/viagem/{viagemId}")
    public ResponseEntity<SolicitacaoColetivaDTO> buscarPorViagem(@PathVariable UUID viagemId) {
        SolicitacaoColetivaDTO dto = service.buscarPorViagemId(viagemId);
        return ResponseEntity.ok(dto);
    }
}