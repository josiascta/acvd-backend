package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoColetivaDTO;
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

    public SolicitacaoColetivaService(SolicitacaoColetivaRepository repository,
                                      UserRepository userRepository,
                                      ViagemRepository viagemRepository,
                                      PdfSolicitacaoColetivaService pdfService,
                                      @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.viagemRepository = viagemRepository;
        this.pdfService = pdfService;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
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

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pdfBytes);
            String hashCalculado = bytesToHex(hashBytes);

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

        try {
            Path filePath = Paths.get(solicitacao.getCaminhoArquivo());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado fisicamente");
            }
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no caminho do arquivo");
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

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String formatarTamanho(long size) {
        if (size < 1024) return size + " B";
        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double)size / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}