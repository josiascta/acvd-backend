package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.PlanejamentoAtividade;
import br.edu.ifpb.ads.acvd.entity.RelatorioAtividade;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfAtividadesTest {

    @Test
    public void devePreencherPlanejamentoAnexoIII() throws IOException {
        PdfPlanejamentoAtividade service = new PdfPlanejamentoAtividade();
        PlanejamentoAtividade dados = new PlanejamentoAtividade();
        dados.setCoordenadoresDaAtividade("Professor X");
        dados.setNumeroParticipantes(20);

        Viagem viagem = new Viagem();
        viagem.setItinerarios(new ArrayList<>());
        dados.setViagem(viagem);

        byte[] pdfBytes = service.preencherPdf(dados);
        assertTrue(pdfBytes.length > 0);
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"));
    }

    @Test
    public void devePreencherRelatorioAnexoVI() throws IOException {
        PdfRelatorioAtividade service = new PdfRelatorioAtividade();
        RelatorioAtividade dados = new RelatorioAtividade();
        dados.setCoordenadoresDaAtividade("Professor Y");
        dados.setRelatorio("Relatório descritivo");

        byte[] pdfBytes = service.preencherPdf(dados);
        assertTrue(pdfBytes.length > 0);
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"));
    }
}