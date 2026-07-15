package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.entity.RelatorioViagemDiscente;
import br.edu.ifpb.ads.acvd.service.RelatorioDiscenteService;
import br.edu.ifpb.ads.acvd.service.PdfSolicitacaoIndividualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID; 

@RestController
@RequestMapping("/api/relatorios-discentes")
@CrossOrigin(origins = "*") 
@PreAuthorize("hasRole('DISCENTE')")
public class RelatorioDiscenteController {

    @Autowired
    private RelatorioDiscenteService service;

    @Autowired
    private PdfSolicitacaoIndividualService pdfService;

    @PostMapping
    public ResponseEntity<RelatorioViagemDiscente> salvar(@RequestBody RelatorioDiscenteDTO dto) {
        RelatorioViagemDiscente relatorio = service.salvar(dto);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/solicitacao/{id}")
    public ResponseEntity<RelatorioViagemDiscente> buscarPorSolicitacao(@PathVariable UUID id) {
        return service.buscarPorSolicitacaoId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdf(@RequestBody RelatorioDiscenteDTO dto) {
        try {
            byte[] pdfBytes = pdfService.preencherAnexoVII(dto);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=anexo-vii.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ====================================================================
    // MÉTODO ATUALIZADO: Baixa o PDF buscando direto da Memória Virtual
    // ====================================================================
    @GetMapping("/solicitacao/{id}/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable UUID id) {
        try {
            // Invoca a Service nova que cria o Resource em memória sob demanda
            Resource resource = service.carregarArquivoRelatorio(id);
            String nomeArquivo = id.toString() + "-relatorio-discente.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                    .body(resource);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Se o relatório não existir no banco, retorna o status correto (ex: 404)
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}