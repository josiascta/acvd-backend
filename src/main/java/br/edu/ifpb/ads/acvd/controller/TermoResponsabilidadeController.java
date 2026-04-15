package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.service.PdfTermoResponsabilidadeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pdf/termo-responsabilidade")
public class TermoResponsabilidadeController {

    private final PdfTermoResponsabilidadeService termoResponsabilidadeService;

    public TermoResponsabilidadeController(PdfTermoResponsabilidadeService termoResponsabilidadeService) {
        this.termoResponsabilidadeService = termoResponsabilidadeService;
    }

    // 1. GERAÇÃO A PARTIR DA SOLICITAÇÃO INDIVIDUAL (Dados já vêm no Body)
    @PostMapping("/individual")
    public ResponseEntity<byte[]> gerarTermoIndividual(@RequestBody SolicitacaoIndividualDTO dados) {
        try {
            byte[] pdfBytes = termoResponsabilidadeService.gerarPdfTermo(dados);
            String nomeArquivo = "Anexo_V_Termo_" + dados.matricula() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. GERAÇÃO A PARTIR DA VIAGEM COLETIVA (Busca dados no BD)
    @GetMapping("/coletiva/{viagemId}/aluno/{alunoId}")
    public ResponseEntity<byte[]> baixarTermoIndividualColetiva(
            @PathVariable UUID viagemId,
            @PathVariable UUID alunoId,
            @RequestParam(required = false) String nomeResp,
            @RequestParam(required = false) String contatoResp
    ) {
        try {
            byte[] pdfBytes = termoResponsabilidadeService.gerarTermoColetivaAdaptado(alunoId, viagemId, nomeResp, contatoResp);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Termo_Individual_" + alunoId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}