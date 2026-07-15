package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SolicitacaoIndividualRepository solicitacaoRepository;
    @Autowired
    private UserRepository userRepository;
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

    // MÉTODO PARA O ANEXO VII (RELATÓRIO DE VIAGEM)
    public byte[] preencherAnexoVII(RelatorioDiscenteDTO dados) throws IOException {
        // BUSCA A SOLICITAÇÃO PARA PEGAR OS DADOS QUE NÃO ESTÃO NO DTO
        SolicitacaoIndividual solicitacao = solicitacaoRepository.findById(dados.solicitacaoId())
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        ClassPathResource pdfResource = new ClassPathResource("anexo-vii.pdf");

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                acroForm.setNeedAppearances(true);

                // 1. IDENTIFICAÇÃO DO DISCENTE (Busca da Entidade Solicitação)
                preencherCampo(acroForm, "nome", solicitacao.getNome());
                preencherCampo(acroForm, "cpf", solicitacao.getCpf());
                preencherCampo(acroForm, "curso", solicitacao.getCurso());
                preencherCampo(acroForm, "matricula", solicitacao.getMatricula());
                preencherCampo(acroForm, "telefone", solicitacao.getTelefone());
                preencherCampo(acroForm, "email", solicitacao.getEmail());

                // 2. INFORMAÇÕES DO AFASTAMENTO (Busca da Entidade Solicitação)
                // Ajuste os métodos getDestino/getOrigem conforme sua classe SolicitacaoIndividual
                preencherCampo(acroForm, "percurso", "João Pessoa-PB / " + solicitacao.getLocalidadeEvento());
                preencherCampo(acroForm, "dataSaida", solicitacao.getDataSaida());
                preencherCampo(acroForm, "horaSaida", solicitacao.getHoraSaida());
                preencherCampo(acroForm, "dataChegada", solicitacao.getDataChegada());
                preencherCampo(acroForm, "horaChegada", solicitacao.getHoraChegada());

                // 3. DESCRIÇÃO SUCINTA E FINANCEIRO (Vem do DTO do Relatório)
                preencherCampo(acroForm, "descricaoAtividades", dados.descricaoAtividades());
                preencherCampo(acroForm, "valorAjudaCusto", dados.valorAjudaCusto() != null ? dados.valorAjudaCusto().toString() : "");
                preencherCampo(acroForm, "ajudaCustoExtenso", dados.ajudaCustoExtenso());
                preencherCampo(acroForm, "valorPassagens", dados.valorPassagens() != null ? dados.valorPassagens().toString() : "");
                preencherCampo(acroForm, "passagensExtenso", dados.passagensExtenso());
                preencherCampo(acroForm, "numeroBilhetes", dados.numeroBilhetes());
                preencherCampo(acroForm, "observacoes", dados.observacoes());

                // 4. ASSINATURA E DATA
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