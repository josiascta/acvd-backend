package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoColetivaDTO;
import br.edu.ifpb.ads.acvd.service.SolicitacaoColetivaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes-coletivas")
@RequiredArgsConstructor
public class SolicitacaoColetivaController {

    private final SolicitacaoColetivaService service;

<<<<<<< HEAD:src/main/java/br/edu/ifpb/ads/acvd/controller/SolicitacaoIndividualController.java
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
=======
    @PostMapping("/gerar-e-salvar")
    public ResponseEntity<SolicitacaoColetivaDTO> gerarESalvar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SolicitacaoColetivaDTO dados) {

        UUID userId = UUID.fromString(jwt.getSubject());

        SolicitacaoColetivaDTO salvo = service.processarSolicitacao(userId, dados);

        return ResponseEntity.ok(salvo);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocumento(@PathVariable UUID id) {
        Resource resource = service.carregarArquivo(id);

        String contentType = "application/pdf";
        try {
            contentType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
        } catch (IOException ex) {}
>>>>>>> develop:src/main/java/br/edu/ifpb/ads/acvd/controller/SolicitacaoColetivaController.java

    // Método auxiliar para evitar repetição de código de cabeçalhos
    private ResponseEntity<Resource> montarRespostaDownload(Resource resource, String prefixo) {
        return ResponseEntity.ok()
<<<<<<< HEAD:src/main/java/br/edu/ifpb/ads/acvd/controller/SolicitacaoIndividualController.java
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + prefixo + resource.getFilename() + "\"")
=======
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"solicitacao_coletiva_" + id + ".pdf\"")
>>>>>>> develop:src/main/java/br/edu/ifpb/ads/acvd/controller/SolicitacaoColetivaController.java
                .body(resource);
    }
   @GetMapping("/minhas")
public ResponseEntity<List<SolicitacaoIndividualDTO>> listarMinhas() {
    List<SolicitacaoIndividualDTO> lista = service.listarPorDiscente();
    return ResponseEntity.ok(lista);
}
}