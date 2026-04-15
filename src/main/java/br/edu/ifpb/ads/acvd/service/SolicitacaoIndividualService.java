package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.RelatorioDiscenteRepository;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class SolicitacaoIndividualService {

    private final RelatorioDiscenteRepository relatorioRepository;
    private final SolicitacaoIndividualRepository repository;
    private final ViagemRepository viagemRepository;
    private final PdfSolicitacaoIndividualService pdfService;
    private final PdfTermoResponsabilidadeService pdfTermoResponsabilidadeService;
    private final Path fileStorageLocation;

    public SolicitacaoIndividualService(SolicitacaoIndividualRepository repository,
                                        ViagemRepository viagemRepository,
                                        PdfSolicitacaoIndividualService pdfService,
                                        RelatorioDiscenteRepository relatorioRepository,
                                        PdfTermoResponsabilidadeService pdfTermoResponsabilidadeService,
                                        @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.repository = repository;
        this.viagemRepository = viagemRepository;
        this.pdfTermoResponsabilidadeService = pdfTermoResponsabilidadeService;
        this.pdfService = pdfService;
        this.relatorioRepository = relatorioRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao criar pasta de uploads", ex);
        }
    }

    @Transactional
    public SolicitacaoIndividualDTO gerarESalvarSolicitacao(SolicitacaoIndividualDTO dto) {
        try {
            Viagem viagem = viagemRepository.findById(dto.viagemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));

            SolicitacaoIndividual solicitacao = repository.findByViagemId(viagem.getId())
                    .orElse(new SolicitacaoIndividual());

            // Mapeamento dos dados (Lógica Individual)
            solicitacao.setViagem(viagem);
            solicitacao.setNome(dto.nome());
            solicitacao.setCpf(dto.cpf());
            solicitacao.setMatricula(dto.matricula());
            solicitacao.setCurso(dto.curso());
            solicitacao.setEmail(dto.email());
            solicitacao.setTelefone(dto.telefone());
            solicitacao.setEndereco(dto.endereco());
            solicitacao.setBanco(dto.banco());
            solicitacao.setAgencia(dto.agencia());
            solicitacao.setConta(dto.conta());
            solicitacao.setJustificativa(dto.justificativa());
            solicitacao.setSolicitadoEm(dto.solicitadoEm());
            solicitacao.setData(new Date());
            solicitacao.setAfastamento(dto.afastamento());
            
            // Auxílios e Período
            solicitacao.setSolicitaInscricao(dto.solicitaInscricao());
            solicitacao.setSolicitaPassagem(dto.solicitaPassagem());
            solicitacao.setSolicitaHospedagem(dto.solicitaHospedagem());
            solicitacao.setSolicitaLocomocao(dto.solicitaLocomocao());
            solicitacao.setSolicitaAlimentacao(dto.solicitaAlimentacao());
            solicitacao.setDataSaida(dto.dataSaida());
            solicitacao.setHoraSaida(dto.horaSaida());
            solicitacao.setDataChegada(dto.dataChegada());
            solicitacao.setHoraChegada(dto.horaChegada());

            // Gerar PDFs
            byte[] bytesAnexoII = pdfService.preencherAnexoII(dto);
            byte[] bytesAnexoV = pdfTermoResponsabilidadeService.gerarPdfTermo(dto);

            Path pathII = this.fileStorageLocation.resolve(viagem.getId() + "_AnexoII.pdf");
            Path pathV = this.fileStorageLocation.resolve(viagem.getId() + "_AnexoV.pdf");

            Files.write(pathII, bytesAnexoII, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(pathV, bytesAnexoV, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            solicitacao.setCaminhoArquivo(pathII.toString());
            solicitacao.setCaminhoArquivoTermo(pathV.toString());
            solicitacao.setHash(gerarHash(bytesAnexoII));
            solicitacao.setTamanho(formatarTamanho(bytesAnexoII.length + bytesAnexoV.length));

            return new SolicitacaoIndividualDTO(repository.save(solicitacao));
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar arquivos", ex);
        }
    }

    public Resource carregarArquivo(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        return carregarRecurso(solicitacao.getCaminhoArquivo());
    }

    public Resource carregarArquivoTermo(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        if (solicitacao.getCaminhoArquivoTermo() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo do Termo (Anexo V) não disponível.");
        }
        return carregarRecurso(solicitacao.getCaminhoArquivoTermo());
    }

    private Resource carregarRecurso(String caminho) {
        try {
            Path filePath = Paths.get(caminho);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) return resource;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo físico não encontrado.");
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Caminho inválido", ex);
        }
    }

    @Transactional
    public void excluir(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        Viagem viagem = solicitacao.getViagem();
        relatorioRepository.findBySolicitacaoId(id).ifPresent(relatorioRepository::delete);

        try {
            if (solicitacao.getCaminhoArquivo() != null) Files.deleteIfExists(Paths.get(solicitacao.getCaminhoArquivo()));
            if (solicitacao.getCaminhoArquivoTermo() != null) Files.deleteIfExists(Paths.get(solicitacao.getCaminhoArquivoTermo()));
        } catch (IOException e) {
            System.err.println("Erro ao deletar arquivos: " + e.getMessage());
        }

        solicitacao.setViagem(null);
        repository.saveAndFlush(solicitacao);
        repository.delete(solicitacao);
        if (viagem != null) viagemRepository.delete(viagem);
    }

    public List<SolicitacaoIndividualDTO> listarPorDiscente() {
        return repository.findAll().stream()
                .filter(s -> s.getViagem() != null)
                .map(SolicitacaoIndividualDTO::new)
                .toList();
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
}