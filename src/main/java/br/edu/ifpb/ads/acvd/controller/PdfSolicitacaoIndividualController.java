package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.service.PdfSolicitacaoIndividualService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pdf/solicitacao-individual")
public class PdfSolicitacaoIndividualController {

    private final PdfSolicitacaoIndividualService pdfService;

    public PdfSolicitacaoIndividualController(PdfSolicitacaoIndividualService pdfService) {
        this.pdfService = pdfService;
    }

    // Recebe os dados via JSON do React, gera o PDF e devolve como Array de Bytes (Download)
    @PostMapping("/gerar")
    public ResponseEntity<byte[]> gerarDocumento(@RequestBody SolicitacaoIndividualDTO dados) {
        try {
            byte[] pdfBytes = pdfService.preencherPdf(dados);

            String nomeArquivo = "solicitacao_individual_" + dados.matricula() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}