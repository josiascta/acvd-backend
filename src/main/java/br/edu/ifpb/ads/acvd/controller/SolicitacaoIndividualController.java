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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes-individuais")
@RequiredArgsConstructor
public class SolicitacaoIndividualController {

    private final SolicitacaoIndividualService service;

    // 1. Gera os DOIS PDFs (Anexo II e V), salva no banco e retorna o DTO
    @PostMapping("/gerar-e-salvar")
    public ResponseEntity<SolicitacaoIndividualDTO> gerarESalvar(@Valid @RequestBody SolicitacaoIndividualDTO dados) {
        SolicitacaoIndividualDTO salvo = service.gerarESalvarSolicitacao(dados);
        return ResponseEntity.ok(salvo);
    }

    // 2. Download do ANEXO II (Solicitação)
    @GetMapping("/{id}/download-solicitacao")
    public ResponseEntity<Resource> downloadSolicitacao(@PathVariable UUID id) {
        Resource resource = service.carregarArquivo(id);
        return montarRespostaDownload(resource, "Solicitacao_Individual_");
    }

    // 3. Download do ANEXO V (Termo de Responsabilidade)
    @GetMapping("/{id}/download-termo")
    public ResponseEntity<Resource> downloadTermo(@PathVariable UUID id) {
        Resource resource = service.carregarArquivoTermo(id); 
        return montarRespostaDownload(resource, "Termo_Responsabilidade_");
    }

    // 4. Listar solicitações do discente logado
    @GetMapping("/minhas")
    public ResponseEntity<List<SolicitacaoIndividualDTO>> listarMinhas() {
        List<SolicitacaoIndividualDTO> lista = service.listarPorDiscente();
        return ResponseEntity.ok(lista);
    }

    // 5. Excluir solicitação e arquivos relacionados
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar para formatar o download do PDF
    private ResponseEntity<Resource> montarRespostaDownload(Resource resource, String prefixo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + prefixo + resource.getFilename() + "\"")
                .body(resource);
    }
}