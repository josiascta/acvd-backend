package br.edu.ifpb.ads.acvd.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import br.edu.ifpb.ads.acvd.entity.Itinerario;
import br.edu.ifpb.ads.acvd.entity.PlanejamentoAtividade;

@Service
public class PdfPlanejamentoAtividade {

    
    public byte[] preencherPdf(PlanejamentoAtividade dados) throws IOException {

<<<<<<< HEAD
        ClassPathResource pdfResource = new ClassPathResource("planejamento-atividade.pdf");
=======
        ClassPathResource pdfResource = new ClassPathResource("solicitacao-coletiva.pdf");
>>>>>>> 5ce9929 (feat: Anexo 3 - Formulario de planejamento de Atividades)

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            if (acroForm != null) {
                preencherCampo(acroForm, "campoCoordenadoresDaAtividade", dados.getCoordenadoresDaAtividade());
                preencherCampo(acroForm, "campoCoordenadoresDePesquisaExtensao", dados.getCoordenadoresDePesquisaExtensao());
                preencherCampo(acroForm, "campoDisciplina", dados.getDisciplina());
                preencherCampo(acroForm, "campoCurso", dados.getCurso());
                preencherCampo(acroForm, "campoTurma", dados.getTurma());
                preencherCampo(acroForm, "campoMetodologia", dados.getMetodologia());
                preencherCampo(acroForm, "campoObjetivos", dados.getObjetivos());
                preencherCampo(acroForm, "campoCargaHorariaCompatibilidade", dados.getCargaHorariaCompatibilidade());
                preencherCampo(acroForm, "campoJustificativaImportancia", dados.getJustificativaImportancia());
                preencherCampo(acroForm, "campoNumeroParticipantes", dados.getNumeroParticipantes().toString());
                preencherCampo(acroForm, "campoItensSeguranca", dados.getItensSeguranca());
                preencherCampo(acroForm, "campoCargaHorariaNoDiarioDeClasse", dados.getCargaHorariaNoDiarioDeClasse());
                preencherCampo(acroForm, "campoContatoDosCoordenadores", dados.getContatoDosCoordenadores());

                List<Itinerario> itinerarios = dados.getViagem().getItinerarios();

                for(int i = 0; i < 7; i++){
<<<<<<< HEAD
                    try {
                        Itinerario itinerario = itinerarios.get(i);
                        preencherCampo(acroForm, "campoAtividadeData" + i, itinerario.getHorarioEntrada().toString());
                        preencherCampo(acroForm, "campoAtividadeLocal" + i, itinerario.getLocal());
                        preencherCampo(acroForm, "campoAtividadeDescricao" + i, itinerario.getDescricao());
                    } catch (IndexOutOfBoundsException e) {
                        // Se não houver itinerário para este índice, preenche os campos com vazio
                        preencherCampo(acroForm, "campoAtividadeData" + i, "");
                        preencherCampo(acroForm, "campoAtividadeLocal" + i, "");
                        preencherCampo(acroForm, "campoAtividadeDescricao" + i, "");

                    }
=======
                    Itinerario itinerario = itinerarios.get(i);
                    preencherCampo(acroForm, "campoAtividadeData" + i, itinerario.getHorarioEntrada().toString());
                    preencherCampo(acroForm, "campoAtividadeDescricao" + i, itinerario.getDescricao());
                    preencherCampo(acroForm, "campoAtividadeLocal" + i, itinerario.getLocal());
>>>>>>> 5ce9929 (feat: Anexo 3 - Formulario de planejamento de Atividades)
                }
                
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
