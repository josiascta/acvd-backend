package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoColetivaDTO;
import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoColetiva;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoColetivaRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Service
public class SolicitacaoColetivaService {

    private final SolicitacaoColetivaRepository repository;
    private final UserRepository userRepository;
    private final ViagemRepository viagemRepository;
    private final PdfSolicitacaoColetivaService pdfService;
    private final Path fileStorageLocation;
    private final PdfSolicitacaoIndividualService pdfIndividualService;

    // NO CONSTRUTOR (Ajuste aqui):
public SolicitacaoColetivaService(SolicitacaoColetivaRepository repository,
                                  UserRepository userRepository,
                                  ViagemRepository viagemRepository,
                                  PdfSolicitacaoColetivaService pdfService,
                                  PdfSolicitacaoIndividualService pdfIndividualService, // 1. ADICIONE ESTE PARÂMETRO
                                  @Value("${app.upload.dir:uploads}") String uploadDir) {
    this.repository = repository;
    this.userRepository = userRepository;
    this.viagemRepository = viagemRepository;
    this.pdfService = pdfService;
    this.pdfIndividualService = pdfIndividualService; // 2. ADICIONE ESTA LINHA
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

    try {
        Files.createDirectories(this.fileStorageLocation);
    } catch (IOException ex) {
        throw new RuntimeException("Erro ao criar pasta de uploads", ex);
    }
}

    @Transactional
    public SolicitacaoColetivaDTO processarSolicitacao(UUID userId, SolicitacaoColetivaDTO dto) {
        SolicitacaoColetiva entidadeSalva = salvarDadosNoBanco(userId, dto);
        SolicitacaoColetiva entidadeComPdf = gerarEAnexarPdf(entidadeSalva);
        return new SolicitacaoColetivaDTO(entidadeComPdf);
    }

    private SolicitacaoColetiva salvarDadosNoBanco(UUID userId, SolicitacaoColetivaDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        SolicitacaoColetiva solicitacao = repository.findByViagemId(viagem.getId())
                .orElse(new SolicitacaoColetiva());

        solicitacao.setViagem(viagem);

        if (solicitacao.getData() == null) {
            solicitacao.setData(new Date());
            solicitacao.setCaminhoArquivo("");
            solicitacao.setTamanho("");
            solicitacao.setHash("");
        }

        solicitacao.setSolicitanteNome(user.getNome());
        solicitacao.setSolicitanteMatricula(user.getMatricula());
        solicitacao.setSolicitanteTelefone(user.getTelefone());
        solicitacao.setSolicitanteEmail(user.getEmail());
        solicitacao.setCurso(user.getCurso());

        solicitacao.setSolicitadoEm(dto.solicitadoEm());
        solicitacao.setAfastamento(dto.afastamento());
        solicitacao.setDisciplinaOuProjeto(dto.disciplinaOuProjeto());
        solicitacao.setSetorDepartamentoCurso(dto.setorDepartamentoCurso());
        solicitacao.setJustificativa(dto.justificativa());

        solicitacao.setInscricao(dto.inscricao());
        solicitacao.setHospedagem(dto.hospedagem());
        solicitacao.setLocomocaoUrbana(dto.locomocaoUrbana());
        solicitacao.setAlimentacao(dto.alimentacao());
        solicitacao.setPassagem(dto.passagem());
        solicitacao.setPlanejamentoVisitaTecnica(dto.planejamentoVisitaTecnica());
        solicitacao.setPlanilha(dto.planilha());
        solicitacao.setTermoResponsabilidade(dto.termoResponsabilidade());
        solicitacao.setOutrosDocumentos(dto.outrosDocumentos());

        return repository.save(solicitacao);
    }

    private SolicitacaoColetiva gerarEAnexarPdf(SolicitacaoColetiva solicitacao) {
        try {
            String caminhoArquivoAntigo = solicitacao.getCaminhoArquivo();
            byte[] pdfBytes = pdfService.preencherPdf(solicitacao);

            String hashCalculado = gerarHash(pdfBytes); // Usando o método que já existe abaixo

            String finalFileName = hashCalculado + ".pdf";
            Path targetLocation = this.fileStorageLocation.resolve(finalFileName);
            Files.write(targetLocation, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            solicitacao.setCaminhoArquivo(targetLocation.toString());
            solicitacao.setTamanho(formatarTamanho(pdfBytes.length));
            solicitacao.setHash(hashCalculado);
            solicitacao.setData(new Date());

            SolicitacaoColetiva solicitacaoSalva = repository.save(solicitacao);

            if (caminhoArquivoAntigo != null && !caminhoArquivoAntigo.isEmpty() && !caminhoArquivoAntigo.equals(targetLocation.toString())) {
                removerDocumentoFisico(caminhoArquivoAntigo);
            }

            return solicitacaoSalva;
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar ou salvar arquivo PDF", ex);
        }
    }

    public Resource carregarArquivo(UUID id) {
        SolicitacaoColetiva solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação Coletiva não encontrada"));
        return carregarRecurso(solicitacao.getCaminhoArquivo());
    }

    private Resource carregarRecurso(String caminho) {
        try {
            Path filePath = Paths.get(caminho);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo físico não encontrado.");
            }
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Caminho do arquivo inválido.", ex);
        }
    }

    private void removerDocumentoFisico(String caminho) {
        if (caminho == null || caminho.isEmpty()) return;
        try {
            Files.deleteIfExists(Paths.get(caminho));
        } catch (IOException e) {
            System.err.println("Erro ao deletar arquivo antigo: " + e.getMessage());
        }
    }

    private String gerarHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String formatarTamanho(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "kB", "MB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    public byte[] gerarTermoResponsabilidadeIndividual(UUID alunoId, UUID viagemId, String nomeResp, String contatoResp) {
    User aluno = userRepository.findById(alunoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    Viagem viagem = viagemRepository.findById(viagemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // MONTAGEM DO DTO (Respeitando os 35 campos do seu record)
    SolicitacaoIndividualDTO dadosParaPdf = new SolicitacaoIndividualDTO(
    // 1-7: IDs e Metadados
    null,                               // id
    viagem.getId(),                     // viagemId
    new java.util.Date(),               // data
    null,                               // caminhoArquivo
    null,                               // caminhoArquivoTermo
    null,                               // tamanho
    null,                               // hash

    // 8-10: Justificativa e Tipos
    "Solicitação via Viagem Coletiva",  // justificativa
    new java.util.Date(),               // solicitadoEm
    null,                               // afastamento (TipoAfastamento)

    // 11-17: Dados do Solicitante
    aluno.getNome(),                    // nome
    aluno.getNumeroCpf(),               // cpf
    aluno.getMatricula(),               // matricula
    aluno.getCurso(),                   // curso
    aluno.getEmail(),                   // email
    aluno.getTelefone(),                // telefone
     "Não informado", // endereco

    // 18-23: CAMPOS DO ANEXO V (Ordem do seu Record)
    "Campus Monteiro",           // campus
    aluno.getTurmaPeriodo(),            // turmaPeriodo
    viagem.getTipoViagem().toString(),  // atividadeEvento
    viagem.getItinerarios().isEmpty() ? "Não informado" : viagem.getItinerarios().get(0).getLocal(), // localidadeEvento
    (aluno.getResponsavelLegal() != null) ? aluno.getResponsavelLegal().getNome() : "Não informado", // nomeFamiliar
    (aluno.getResponsavelLegal() != null) ? aluno.getResponsavelLegal().getContato() : "Não informado", // contatoFamiliar

    // 24-26: Dados Bancários
    null,                               // banco
    null,                               // agencia
    null,                               // conta

    // 27-31: Auxílios (5 Booleans)
    false,                              // solicitaInscricao
    false,                              // solicitaPassagem
    false,                              // solicitaHospedagem
    false,                              // solicitaLocomocao
    false,                              // solicitaAlimentacao

    // 32-35: Período de Afastamento (4 Strings)
    (viagem.getDataPartida() != null) ? viagem.getDataPartida().toString() : "", // dataSaida
    "",                                 // horaSaida
    (viagem.getDataRetorno() != null) ? viagem.getDataRetorno().toString() : "", // dataChegada
    ""                                  // horaChegada
);

    try {
        // Agora o 'pdfIndividualService' vai conseguir preencher o Anexo V
        return pdfIndividualService.preencherAnexoV(dadosParaPdf);
    } catch (IOException e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar PDF");
    }
}

    
}