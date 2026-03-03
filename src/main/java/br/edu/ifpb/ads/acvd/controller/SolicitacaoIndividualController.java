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

import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes-individuais")
@RequiredArgsConstructor
public class SolicitacaoIndividualController {

    private final SolicitacaoIndividualService service;

    // 1. Gera os DOIS PDFs, salva no banco e retorna o DTO com os caminhos
    @PostMapping("/gerar-e-salvar")
    public ResponseEntity<SolicitacaoIndividualDTO> gerarESalvar(@Valid @RequestBody SolicitacaoIndividualDTO dados) {
        // O service agora cuida de disparar preencherAnexoII e preencherAnexoV
        SolicitacaoIndividualDTO salvo = service.gerarESalvarSolicitacao(dados);
        return ResponseEntity.ok(salvo);
    }

    // 2. Download do ANEXO II (Solicitação)
    @GetMapping("/{id}/download-solicitacao")
    public ResponseEntity<Resource> downloadSolicitacao(@PathVariable UUID id) {
        Resource resource = service.carregarArquivo(id); // Método padrão para o arquivo principal
        return montarRespostaDownload(resource, "Solicitacao_Individual_");
    }

    // 3. Download do ANEXO V (Termo de Responsabilidade)
    // Você precisará criar o método carregarArquivoTermo no Service para ler o outro caminho
    @GetMapping("/{id}/download-termo")
    public ResponseEntity<Resource> downloadTermo(@PathVariable UUID id) {
        Resource resource = service.carregarArquivoTermo(id); 
        return montarRespostaDownload(resource, "Termo_Responsabilidade_");
    }

    // Método auxiliar para evitar repetição de código de cabeçalhos
    private ResponseEntity<Resource> montarRespostaDownload(Resource resource, String prefixo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + prefixo + resource.getFilename() + "\"")
                .body(resource);
    }
}