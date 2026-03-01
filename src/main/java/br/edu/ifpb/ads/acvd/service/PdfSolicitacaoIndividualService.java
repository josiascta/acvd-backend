package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

@Service
public class PdfSolicitacaoIndividualService {

    public byte[] preencherPdf(SolicitacaoIndividualDTO dados) throws IOException {

        // 1. Carrega o template PDF que deve estar na pasta src/main/resources/
        ClassPathResource pdfResource = new ClassPathResource("solicitacao-individual.pdf");

        // Utilizando Loader.loadPDF() que é o padrão do PDFBox 3.x
        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            if (acroForm != null) {
                // 2. Preenche os campos de texto com os dados do DTO
                preencherCampo(acroForm, "campoNome", dados.nome());
                preencherCampo(acroForm, "campoMatricula", dados.matricula());
                preencherCampo(acroForm, "campoCurso", dados.curso());
                preencherCampo(acroForm, "campoEmail", dados.email());
                preencherCampo(acroForm, "campoTelefone", dados.telefone());
                preencherCampo(acroForm, "campoJustificativa", dados.justificativa());

                // 3. Formatar a data para o padrão de Portugal/Brasil (dd/MM/yyyy)
                String dataFormatada = "";
                if (dados.solicitadoEm() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    dataFormatada = sdf.format(dados.solicitadoEm());
                }
                preencherCampo(acroForm, "campoSolicitadoEm", dataFormatada);

                // 4. Achatar o formulário (Flatten)
                // Isto transforma os campos de formulário em texto estático,
                // impedindo que o documento seja editado posteriormente noutros leitores de PDF.
                acroForm.flatten();
            }

            // 5. Guarda as alterações no outputStream e devolve os bytes
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void preencherCampo(PDAcroForm form, String nomeCampo, String valor) throws IOException {
        PDField field = form.getField(nomeCampo);
        if (field != null) {
            // Se o valor for nulo, insere uma string vazia para não dar erro
            field.setValue(valor != null ? valor : "");
        } else {
            // Mensagem útil na consola para verificar se o nome do campo no código
            // não corresponde exatamente ao nome que está no ficheiro PDF.
            System.err.println("AVISO: Campo não encontrado no template PDF: " + nomeCampo);
        }
    }
}