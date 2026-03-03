package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.service.PdfSolicitacaoIndividualService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf") // Mudei para api/pdf para ficar mais organizado
public class PdfSolicitacaoIndividualController {

    private final PdfSolicitacaoIndividualService pdfService;

    public PdfSolicitacaoIndividualController(PdfSolicitacaoIndividualService pdfService) {
        this.pdfService = pdfService;
    }

    // DOWNLOAD DO ANEXO II (SOLICITAÇÃO)
    @PostMapping("/gerar-solicitacao")
    public ResponseEntity<byte[]> gerarSolicitacao(@RequestBody SolicitacaoIndividualDTO dados) {
        try {
            // Chamando o método renomeado no service
            byte[] pdfBytes = pdfService.preencherAnexoII(dados);

            String nomeArquivo = "Anexo_II_Solicitacao_" + dados.matricula() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DOWNLOAD DO ANEXO V (TERMO DE RESPONSABILIDADE)
    @PostMapping("/gerar-termo")
    public ResponseEntity<byte[]> gerarTermo(@RequestBody SolicitacaoIndividualDTO dados) {
        try {
            // Chamando o NOVO método no service
            byte[] pdfBytes = pdfService.preencherAnexoV(dados);

            String nomeArquivo = "Anexo_V_Termo_" + dados.matricula() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}