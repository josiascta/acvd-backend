package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.service.SolicitacaoIndividualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes-individuais")
@RequiredArgsConstructor
public class SolicitacaoIndividualController {

    private final SolicitacaoIndividualService service;

    // Recebe os dados, gera o PDF, salva fisicamente, registra no BD e devolve o DTO atualizado
    @PostMapping("/gerar-e-salvar")
    public ResponseEntity<SolicitacaoIndividualDTO> gerarESalvar(@Valid @RequestBody SolicitacaoIndividualDTO dados) {
        SolicitacaoIndividualDTO salvo = service.gerarESalvarSolicitacao(dados);
        return ResponseEntity.ok(salvo);
    }

    // Endpoint para fazer o download do PDF gerado e salvo
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocumento(@PathVariable UUID id) {
        Resource resource = service.carregarArquivo(id);

        String contentType = "application/pdf";
        try {
            contentType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
        } catch (IOException ex) {
            // Ignorar
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"solicitacao_" + id + ".pdf\"")
                .body(resource);
    }
}