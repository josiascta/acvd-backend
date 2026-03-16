package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.entity.RelatorioViagemDiscente;
import br.edu.ifpb.ads.acvd.service.RelatorioDiscenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID; 

@RestController
@RequestMapping("/api/relatorios-discentes")
@CrossOrigin(origins = "*") // Recomendado para integração com o React
public class RelatorioDiscenteController {

    @Autowired
    private RelatorioDiscenteService service;

    @PostMapping
    public ResponseEntity<RelatorioViagemDiscente> salvar(@RequestBody RelatorioDiscenteDTO dto) {
        // Salva ou atualiza os dados do Anexo VII [cite: 6]
        RelatorioViagemDiscente relatorio = service.salvar(dto);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/solicitacao/{id}")
    public ResponseEntity<RelatorioViagemDiscente> buscarPorSolicitacao(@PathVariable UUID id) {
        // Altere o tipo de 'Long' para 'UUID' para bater com o Service
        return service.buscarPorSolicitacaoId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @Autowired
    private br.edu.ifpb.ads.acvd.service.PdfSolicitacaoIndividualService pdfService;

    @PostMapping("/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdf(@RequestBody RelatorioDiscenteDTO dto) {
        try {
            // Chama o serviço que preenche o Anexo VII
            byte[] pdfBytes = pdfService.preencherAnexoVII(dto);

            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=anexo-vii.pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/solicitacao/{id}/pdf")
public ResponseEntity<org.springframework.core.io.Resource> downloadPdf(@PathVariable UUID id) {
    try {
        // O nome do arquivo que salvamos no Service
        String nomeArquivo = id.toString() + "-relatorio-discente.pdf";
        java.nio.file.Path path = java.nio.file.Paths.get("uploads/" + nomeArquivo);
        
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

        if (resource.exists()) {
            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
}