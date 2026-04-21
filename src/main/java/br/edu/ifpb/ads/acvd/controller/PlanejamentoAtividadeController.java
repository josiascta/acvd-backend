package br.edu.ifpb.ads.acvd.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpb.ads.acvd.dto.PlanejamentoAtividadeDTO;
import br.edu.ifpb.ads.acvd.service.PlanejamentoAtividadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/planejamento-atividade")
@RequiredArgsConstructor
public class PlanejamentoAtividadeController {
    
    private final PlanejamentoAtividadeService service;

    @PostMapping("/gerar-e-salvar")
    public ResponseEntity<PlanejamentoAtividadeDTO> gerarESalvar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PlanejamentoAtividadeDTO dados) {

        UUID userId = UUID.fromString(jwt.getSubject());

        PlanejamentoAtividadeDTO salvo = service.processarPlanejamento(userId, dados);

        return ResponseEntity.ok(salvo);
    }

    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocumento(@PathVariable UUID id) {
        Resource resource = service.carregarArquivo(id);

        String contentType = "application/pdf";
        try {
            contentType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
        } catch (IOException ex) {}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
<<<<<<< HEAD
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"planejamento_atividade_" + id + ".pdf\"")
=======
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"solicitacao_coletiva_" + id + ".pdf\"")
>>>>>>> 5ce9929 (feat: Anexo 3 - Formulario de planejamento de Atividades)
                .body(resource);
    }
}
