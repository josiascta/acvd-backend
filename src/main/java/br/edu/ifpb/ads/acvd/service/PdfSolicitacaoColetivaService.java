package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoColetiva;
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
import java.util.Calendar;

@Service
public class PdfSolicitacaoColetivaService {

    public byte[] preencherPdf(SolicitacaoColetiva dados) throws IOException {

        ClassPathResource pdfResource = new ClassPathResource("solicitacao-coletiva.pdf");

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            if (acroForm != null) {
                preencherCampo(acroForm, "campoNome", dados.getSolicitanteNome());
                preencherCampo(acroForm, "campoCoord", dados.getSetorDepartamentoCurso());
                preencherCampo(acroForm, "campoCurso", dados.getCurso());
                preencherCampo(acroForm, "campoEmail", dados.getSolicitanteEmail());
                preencherCampo(acroForm, "campoMatricula", dados.getSolicitanteMatricula());
                preencherCampo(acroForm, "campoTelefone", dados.getSolicitanteTelefone());
                preencherCampo(acroForm, "campoJustificativa", dados.getJustificativa());

                if (dados.getSolicitadoEm() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dados.getSolicitadoEm());

                    String dia = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
                    String mes = String.format("%02d", cal.get(Calendar.MONTH) + 1);
                    String ano = String.valueOf(cal.get(Calendar.YEAR));

                    preencherCampo(acroForm, "dataSolicitadoEmDia", dia);
                    preencherCampo(acroForm, "dataSolicitadoEmMes", mes);
                    preencherCampo(acroForm, "dataSolicitadoEmAno", ano);
                }

                if (dados.isInscricao()) preencherCampo(acroForm, "checkInscricao", "Yes_qsbc");
                if (dados.isHospedagem()) preencherCampo(acroForm, "checkHospedagem", "Yes_rnfp");
                if (dados.isLocomocaoUrbana()) preencherCampo(acroForm, "checkLocomocao", "Yes_xccu");
                if (dados.isAlimentacao()) preencherCampo(acroForm, "checkAlimentacao", "Yes_jjli");
                if (dados.isPassagem()) preencherCampo(acroForm, "checkPassagem", "Yes_rtpd");

                if (dados.isPlanejamentoVisitaTecnica()) preencherCampo(acroForm, "checkPlanejamentoVisitaTecnica", "Yes_ftvq");
                if (dados.isPlanilha()) preencherCampo(acroForm, "checkPlanilha", "Yes_qvio");
                if (dados.isTermoResponsabilidade()) preencherCampo(acroForm, "checkTermoResponsabilidade", "Yes_waka");
                if (dados.isOutrosDocumentos()) preencherCampo(acroForm, "checkOutrosDocumentos", "Yes_zwoe");

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