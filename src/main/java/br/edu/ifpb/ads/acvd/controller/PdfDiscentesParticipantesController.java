package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.DiscenteParticipanteDTO;
import br.edu.ifpb.ads.acvd.service.PdfDiscentesParticipantesService;
import br.edu.ifpb.ads.acvd.service.RequisicaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfDiscentesParticipantesController {

    private final PdfDiscentesParticipantesService pdfService;
    private final RequisicaoService requisicaoService;

    @GetMapping("/viagens/{viagemId}/discentes-participantes")
    public ResponseEntity<byte[]> gerarPdfDiscentesParticipantes(@PathVariable UUID viagemId) {
        try {
            List<DiscenteParticipanteDTO> discentes = requisicaoService.listarDiscentesParticipantes(viagemId);

            byte[] pdfBytes = pdfService.preencherAnexoIV(discentes);

            String nomeArquivo = "Anexo_IV_Discentes_Participantes_" + viagemId + ".pdf";

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