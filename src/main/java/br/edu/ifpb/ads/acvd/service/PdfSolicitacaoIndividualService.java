package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Service
public class PdfSolicitacaoIndividualService {

    // MÉTODO PARA O ANEXO II (SOLICITAÇÃO)
    public byte[] preencherAnexoII(SolicitacaoIndividualDTO dados) throws IOException {
        ClassPathResource pdfResource = new ClassPathResource("solicitacao-individual.pdf");

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                acroForm.setNeedAppearances(true);
                acroForm.getFields().forEach(field -> 
            System.out.println("NOME NO ANEXO II: " + field.getFullyQualifiedName())
        );

                // Dados Pessoais
                preencherCampo(acroForm, "campoNome", dados.nome());
                preencherCampo(acroForm, "campoCPF", dados.cpf());
                preencherCampo(acroForm, "campocpf", dados.cpf());
                preencherCampo(acroForm, "campoCpf", dados.cpf());
                preencherCampo(acroForm, "campoMatricula", dados.matricula());
                preencherCampo(acroForm, "campoCurso", dados.curso());
                preencherCampo(acroForm, "campoEmail", dados.email());
                preencherCampo(acroForm, "campoTelefone", dados.telefone());
                preencherCampo(acroForm, "campoEndereco", dados.endereco());

                // Dados Bancários
                preencherCampo(acroForm, "campoBanco", dados.banco());
                preencherCampo(acroForm, "campoAgencia", dados.agencia());
                preencherCampo(acroForm, "campoConta", dados.conta());

                // Checkboxes
                marcarCheckbox(acroForm, "checkInscricao", dados.solicitaInscricao());
                marcarCheckbox(acroForm, "checkPassagem", dados.solicitaPassagem());
                marcarCheckbox(acroForm, "checkHospedagem", dados.solicitaHospedagem());
                marcarCheckbox(acroForm, "checkLocomocao", dados.solicitaLocomocao());
                marcarCheckbox(acroForm, "checkAlimentacao", dados.solicitaAlimentacao());

                // Datas e Justificativa
                preencherCampo(acroForm, "campoJustificativa", dados.justificativa());
                preencherCampo(acroForm, "campoDataSaida", dados.dataSaida());
                preencherCampo(acroForm, "campoHoraSaida", dados.horaSaida());
                preencherCampo(acroForm, "campoDataChegada", dados.dataChegada());
                preencherCampo(acroForm, "campoHoraChegada", dados.horaChegada());

                if (dados.solicitadoEm() != null) {
                    preencherCampo(acroForm, "campoSolicitadoEm", new SimpleDateFormat("dd/MM/yyyy").format(dados.solicitadoEm()));
                }

                tentarFlatten(acroForm);
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    // MÉTODO PARA O ANEXO V (TERMO DE RESPONSABILIDADE)
    public byte[] preencherAnexoV(SolicitacaoIndividualDTO dados) throws IOException {
        // Ajuste o caminho se não estiver na pasta templates
        ClassPathResource pdfResource = new ClassPathResource("anexo-v.pdf");

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                acroForm.setNeedAppearances(true);
                acroForm.getFields().forEach(field -> 
                System.out.println("NOME REAL NO PDF: " + field.getFullyQualifiedName()));

                // Cabeçalho e Identificação
                preencherCampo(acroForm, "nomeAluno", dados.nome());
                preencherCampo(acroForm, "curso", dados.curso());
                preencherCampo(acroForm, "matricula", dados.matricula());
                preencherCampo(acroForm, "campus", dados.campus());
                preencherCampo(acroForm, "turma", dados.turmaPeriodo());

                // Atividade
                preencherCampo(acroForm, "atividade", dados.atividadeEvento());
                preencherCampo(acroForm, "local", dados.localidadeEvento());
                String periodo = "de " + dados.dataSaida() + " a " + dados.dataChegada();
                preencherCampo(acroForm, "periodo", periodo);

                // Familiar
                preencherCampo(acroForm, "nomeFamiliar", dados.nomeFamiliar());
                preencherCampo(acroForm, "contatoFamiliar", dados.contatoFamiliar());

                // Data automática (Substitui o atributo que removemos)
                String dataHoje = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR")).format(new Date());
                preencherCampo(acroForm, "cidadeData", "João Pessoa-PB, " + dataHoje);

                tentarFlatten(acroForm);
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void preencherCampo(PDAcroForm form, String nomeCampo, String valor) throws IOException {
        PDField field = form.getField(nomeCampo);
        if (field != null) field.setValue(valor != null ? valor : "");
    }

    private void marcarCheckbox(PDAcroForm form, String nomeCampo, Boolean selecionado) throws IOException {
        PDField field = form.getField(nomeCampo);
        if (field instanceof PDCheckBox checkBox) {
            if (Boolean.TRUE.equals(selecionado)) {
                String onValue = checkBox.getOnValues().iterator().next();
                checkBox.setValue(onValue);
            } else {
                checkBox.unCheck();
            }
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