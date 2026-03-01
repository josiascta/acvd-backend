package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
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
public class SolicitacaoIndividualService {

    private final SolicitacaoIndividualRepository repository;
    private final ViagemRepository viagemRepository;
    private final PdfSolicitacaoIndividualService pdfService;
    private final Path fileStorageLocation;

    public SolicitacaoIndividualService(SolicitacaoIndividualRepository repository,
                                        ViagemRepository viagemRepository,
                                        PdfSolicitacaoIndividualService pdfService,
                                        @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.repository = repository;
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
    public SolicitacaoIndividualDTO gerarESalvarSolicitacao(SolicitacaoIndividualDTO dto) {
        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        // Se já existir uma solicitação individual para essa viagem, exclui o arquivo antigo (opcional dependendo da sua regra)
        repository.findByViagemId(viagem.getId()).ifPresent(antiga -> {
            removerDocumentoFisico(antiga.getCaminhoArquivo());
            repository.delete(antiga);
            repository.flush();
        });

        try {
            // 1. Gerar o PDF com os dados
            byte[] pdfBytes = pdfService.preencherPdf(dto);

            // 2. Calcular o Hash SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pdfBytes);
            String hashCalculado = bytesToHex(hashBytes);

            // 3. Salvar o arquivo fisicamente usando o Hash como nome
            String finalFileName = hashCalculado + ".pdf";
            Path targetLocation = this.fileStorageLocation.resolve(finalFileName);
            Files.write(targetLocation, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 4. Salvar os dados no Banco de Dados
            SolicitacaoIndividual solicitacao = new SolicitacaoIndividual();
            solicitacao.setViagem(viagem);
            solicitacao.setCaminhoArquivo(targetLocation.toString());
            solicitacao.setTamanho(formatarTamanho(pdfBytes.length));
            solicitacao.setHash(hashCalculado);
            solicitacao.setData(new Date());

            // Dados Específicos
            solicitacao.setJustificativa(dto.justificativa());
            solicitacao.setSolicitadoEm(dto.solicitadoEm());
            solicitacao.setAfastamento(dto.afastamento());
            solicitacao.setNome(dto.nome());
            solicitacao.setMatricula(dto.matricula());
            solicitacao.setCurso(dto.curso());
            solicitacao.setEmail(dto.email());
            solicitacao.setTelefone(dto.telefone());

            return new SolicitacaoIndividualDTO(repository.save(solicitacao));

        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar e salvar a solicitação", ex);
        }
    }

    public Resource carregarArquivo(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

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
        if (caminho == null) return;
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