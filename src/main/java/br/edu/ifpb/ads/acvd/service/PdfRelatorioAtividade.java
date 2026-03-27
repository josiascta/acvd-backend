package br.edu.ifpb.ads.acvd.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import br.edu.ifpb.ads.acvd.entity.RelatorioAtividade;

@Service
public class PdfRelatorioAtividade {

    public byte[] preencherPdf(RelatorioAtividade dados) throws IOException {

        ClassPathResource pdfResource = new ClassPathResource("relatorio-atividade.pdf");

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            if (acroForm != null) {
                preencherCampo(acroForm, "campoCoordenadores", dados.getCoordenadoresDaAtividade());
                preencherCampo(acroForm, "campoDisciplina", dados.getDisciplinaOuProjeto());
                preencherCampo(acroForm, "campoRelatorio", dados.getRelatorio());
                preencherCampo(acroForm, "campoConsideracoesFinais", dados.getConsideracoesFinais());
                preencherCampo(acroForm, "campoContatoInstituicao", dados.getContatoDaInstituicao());
                
                acroForm.flatten();
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
            System.err.println("AVISO: Campo não encontrado no PDF Coletivo: " + nomeCampo);
        }
    }
}
