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

    // 1. Gera e salva (Sem alterações)
    @PostMapping("/gerar-e-salvar")
    public ResponseEntity<SolicitacaoIndividualDTO> gerarESalvar(@Valid @RequestBody SolicitacaoIndividualDTO dados) {
        SolicitacaoIndividualDTO salvo = service.gerarESalvarSolicitacao(dados);
        return ResponseEntity.ok(salvo);
    }

    // 2. Download do ANEXO II (Modificado internamente para evitar erros de leitura de disco)
    @GetMapping("/{id}/download-solicitacao")
    public ResponseEntity<Resource> downloadSolicitacao(@PathVariable UUID id) {
        Resource resource = service.carregarArquivo(id);
        // Passamos o nome do arquivo fixo direto aqui para não quebrar no resource.getFilename()
        return montarRespostaDownload(resource, "Solicitacao_Individual_" + id + ".pdf");
    }

    // 3. Download do ANEXO V (Modificado internamente para evitar erros de leitura de disco)
    @GetMapping("/{id}/download-termo")
    public ResponseEntity<Resource> downloadTermo(@PathVariable UUID id) {
        Resource resource = service.carregarArquivoTermo(id); 
        return montarRespostaDownload(resource, "Termo_Responsabilidade_" + id + ".pdf");
    }

    // 4. Listar (Sem alterações)
    @GetMapping("/minhas")
    public ResponseEntity<List<SolicitacaoIndividualDTO>> listarMinhas() {
        List<SolicitacaoIndividualDTO> lista = service.listarPorDiscente();
        return ResponseEntity.ok(lista);
    }

    // 5. Excluir (Sem alterações)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar adaptado para receber o nome final pronto do arquivo
    private ResponseEntity<Resource> montarRespostaDownload(Resource resource, String nomeArquivoCompleto) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivoCompleto + "\"")
                .body(resource);
    }
}