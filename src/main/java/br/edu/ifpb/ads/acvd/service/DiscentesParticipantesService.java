package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.DiscenteParticipanteDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class DiscentesParticipantesService {

    private static final int LIMITE_POR_PAGINA = 25;
    private static final String TEMPLATE_PDF = "discentes-participantes.pdf";

    public byte[] preencherAnexoIV(List<DiscenteParticipanteDTO> discentes, String cidade) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();

        try (PDDocument documentoFinal = new PDDocument();
             ByteArrayOutputStream outputStreamFinal = new ByteArrayOutputStream()) {

            int totalPaginas = (int) Math.ceil((double) discentes.size() / LIMITE_POR_PAGINA);

            if (totalPaginas == 0) {
                totalPaginas = 1;
            }

            for (int paginaAtual = 0; paginaAtual < totalPaginas; paginaAtual++) {
                int fromIndex = paginaAtual * LIMITE_POR_PAGINA;
                int toIndex = Math.min(fromIndex + LIMITE_POR_PAGINA, discentes.size());

                List<DiscenteParticipanteDTO> discentesPagina = discentes.subList(fromIndex, toIndex);

                byte[] paginaPreenchida = preencherPagina(discentesPagina, fromIndex, cidade);

                try (PDDocument paginaDoc = Loader.loadPDF(paginaPreenchida)) {
                    pdfMerger.appendDocument(documentoFinal, paginaDoc);
                }
            }

            documentoFinal.save(outputStreamFinal);
            return outputStreamFinal.toByteArray();
        }
    }

    private byte[] preencherPagina(List<DiscenteParticipanteDTO> discentesPagina, int offsetGlobal, String cidade) throws IOException {
        ClassPathResource pdfResource = new ClassPathResource(TEMPLATE_PDF);

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes()); // Padrão PDFBox 3.x
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                acroForm.setNeedAppearances(true);

                if (cidade != null && !cidade.isBlank()) {
                    preencherCampo(acroForm, "campoCidade", cidade);
                }

                int indexFormulario = 1;

                for (DiscenteParticipanteDTO discente : discentesPagina) {

                    int numeroSequencialGlobal = offsetGlobal + indexFormulario;
                    String sufixo = String.format("%02d", indexFormulario);

                    preencherCampo(acroForm, "seq_" + indexFormulario, String.valueOf(numeroSequencialGlobal));

                    preencherCampo(acroForm, "campoNome" + sufixo, discente.nome());
                    preencherCampo(acroForm, "campoMatricula" + sufixo, discente.matricula());
                    preencherCampo(acroForm, "campoCPF" + sufixo, discente.cpf());
                    preencherCampo(acroForm, "campoBanco" + sufixo, discente.banco());
                    preencherCampo(acroForm, "campoAgencia" + sufixo, discente.agencia());
                    preencherCampo(acroForm, "campoOp" + sufixo, discente.op());
                    preencherCampo(acroForm, "campoConta" + sufixo, discente.conta());

                    indexFormulario++;
                }

                tentarFlatten(acroForm);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void preencherCampo(PDAcroForm form, String nomeCampo, String valor) throws IOException {
        PDField field = form.getField(nomeCampo);
        if (field == null && "campoNome04".equals(nomeCampo)) {
            field = form.getField("campoNome004");
        }

        if (field != null) {
            field.setValue(valor != null ? valor : "");
        } else {
            System.err.println("Aviso: Campo não encontrado no PDF de Discentes Participantes: " + nomeCampo);
        }
    }

    private void tentarFlatten(PDAcroForm acroForm) {
        try {
            acroForm.flatten();
        } catch (IOException e) {
            System.err.println("Aviso: Falha ao achatar PDF: " + e.getMessage());
        }
    }
}