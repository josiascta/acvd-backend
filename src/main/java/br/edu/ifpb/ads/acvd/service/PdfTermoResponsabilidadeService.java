package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Service
public class PdfTermoResponsabilidadeService {

    private final UserRepository userRepository;
    private final ViagemRepository viagemRepository;

    public PdfTermoResponsabilidadeService(UserRepository userRepository, ViagemRepository viagemRepository) {
        this.userRepository = userRepository;
        this.viagemRepository = viagemRepository;
    }

    public byte[] gerarPdfTermo(SolicitacaoIndividualDTO dados) throws IOException {
        Date dataNasc = userRepository.findByMatricula(dados.matricula())
                .map(User::getDataNascimento)
                .orElse(null);

        ClassPathResource pdfResource = new ClassPathResource("anexo-v.pdf");

        try (InputStream is = pdfResource.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                acroForm.setNeedAppearances(true);

                int idade = 0;
                if (dataNasc != null) {
                    java.time.LocalDate nascimento = dataNasc.toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    idade = java.time.Period.between(nascimento, java.time.LocalDate.now()).getYears();
                }

                preencherCampo(acroForm, "nomeAluno", dados.nome());
                preencherCampo(acroForm, "curso", dados.curso());
                preencherCampo(acroForm, "matricula", dados.matricula());
                preencherCampo(acroForm, "campus", dados.campus());
                preencherCampo(acroForm, "turma", dados.turmaPeriodo());
                preencherCampo(acroForm, "atividade", dados.atividadeEvento());
                preencherCampo(acroForm, "local", dados.localidadeEvento());
                preencherCampo(acroForm, "periodo", "de " + dados.dataSaida() + " a " + dados.dataChegada());

                if (idade >= 18) {
                    preencherCampo(acroForm, "contatoMaior", dados.telefone());
                    preencherCampo(acroForm, "nomeFamiliar", "");
                    preencherCampo(acroForm, "contatoFamiliar", "");
                } else {
                    preencherCampo(acroForm, "nomeFamiliar", dados.nomeFamiliar());
                    preencherCampo(acroForm, "contatoFamiliar", dados.contatoFamiliar());
                    preencherCampo(acroForm, "contatoMaior", "");
                }

                String dataHoje = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR")).format(new Date());
                preencherCampo(acroForm, "cidadeData", "Monteiro-PB, " + dataHoje);

                tentarFlatten(acroForm);
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    // --- MÉTODO AUXILIAR PARA A COLETIVA ---
    public byte[] gerarTermoColetivaAdaptado(UUID alunoId, UUID viagemId, String nomeResp, String contatoResp) throws IOException {
        User aluno = userRepository.findById(alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));

        Viagem viagem = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));

        SolicitacaoIndividualDTO dadosParaPdf = new SolicitacaoIndividualDTO(
                null, viagem.getId(), new Date(), null, null, null, null,
                "Solicitação via Viagem Coletiva", new Date(), null,
                aluno.getNome(), aluno.getNumeroCpf(), aluno.getMatricula(), aluno.getCurso(),
                aluno.getEmail(), aluno.getTelefone(), "Não informado",
                "Campus Monteiro", aluno.getTurmaPeriodo(), viagem.getTipoViagem().toString(),
                viagem.getItinerarios().isEmpty() ? "Não informado" : viagem.getItinerarios().get(0).getLocal(),
                (aluno.getResponsavelLegal() != null) ? aluno.getResponsavelLegal().getNome() : nomeResp,
                (aluno.getResponsavelLegal() != null) ? aluno.getResponsavelLegal().getContato() : contatoResp,
                null, null, null,
                false, false, false, false, false,
                (viagem.getDataPartida() != null) ? viagem.getDataPartida().toString() : "", "",
                (viagem.getDataRetorno() != null) ? viagem.getDataRetorno().toString() : "", ""
        );

        return gerarPdfTermo(dadosParaPdf);
    }

    private void preencherCampo(PDAcroForm form, String nomeCampo, String valor) throws IOException {
        PDField field = form.getField(nomeCampo);
        if (field != null) field.setValue(valor != null ? valor : "");
    }

    private void tentarFlatten(PDAcroForm acroForm) {
        try {
            acroForm.flatten();
        } catch (IOException e) {
            System.err.println("Aviso: Falha ao achatar PDF: " + e.getMessage());
        }
    }
}