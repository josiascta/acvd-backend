package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.DiscenteParticipanteDTO;
import org.apache.pdfbox.Loader;
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
public class PdfDiscentesParticipantesService {

    public byte[] preencherAnexoIV(List<DiscenteParticipanteDTO> discentes) throws IOException {
        ClassPathResource pdfResource = new ClassPathResource("discentes-participantes.pdf");

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                acroForm.setNeedAppearances(true);


                int index = 1;
                for (DiscenteParticipanteDTO discente : discentes) {

                    String sufixo = String.format("%02d", index);

                    preencherCampo(acroForm, "campoNome" + sufixo, discente.nome());
                    preencherCampo(acroForm, "campoMatricula" + sufixo, discente.matricula());
                    preencherCampo(acroForm, "campoCPF" + sufixo, discente.cpf());
                    preencherCampo(acroForm, "campoBanco" + sufixo, discente.banco());
                    preencherCampo(acroForm, "campoAgencia" + sufixo, discente.agencia());
                    preencherCampo(acroForm, "campoOp" + sufixo, discente.op());
                    preencherCampo(acroForm, "campoConta" + sufixo, discente.conta());

                    index++;
                }

                tentarFlatten(acroForm);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void preencherCampo(PDAcroForm form, String nomeCampo, String valor) throws IOException {
        PDField field = form.getField(nomeCampo);
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