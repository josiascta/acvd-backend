package br.edu.ifpb.ads.acvd.service;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.ads.acvd.dto.RelatorioAtividadeDTO;
import br.edu.ifpb.ads.acvd.entity.RelatorioAtividade;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.RelatorioAtividadeRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;

@Service
public class RelatorioAtividadeService {
    
    private final RelatorioAtividadeRepository repository;
    private final ViagemRepository viagemRepository;
    private final Path fileStorageLocation;
    private final PdfRelatorioAtividade pdfService;

        public RelatorioAtividadeService(RelatorioAtividadeRepository repository,
                                      ViagemRepository viagemRepository,
                                      PdfRelatorioAtividade pdfService,
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
    public RelatorioAtividadeDTO processarRelatorio(UUID userId, RelatorioAtividadeDTO dto) {
        RelatorioAtividade entidadeSalva = salvarDadosNoBanco(userId, dto);
        RelatorioAtividade entidadeComPdf = gerarEAnexarPdf(entidadeSalva);
        return new RelatorioAtividadeDTO(entidadeComPdf);
    }

    
    private RelatorioAtividade salvarDadosNoBanco(UUID userId, RelatorioAtividadeDTO dto) {
        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        RelatorioAtividade relatorio = repository.findByViagemId(viagem.getId())
                .orElse(new RelatorioAtividade());

        relatorio.setViagem(viagem);

        if (relatorio.getData() == null) {
            relatorio.setData(new Date());
            relatorio.setCaminhoArquivo("");
            relatorio.setTamanho("");
            relatorio.setHash("");
        }

        relatorio.setCoordenadoresDaAtividade(dto.coordenadoresDaAtividade());
        relatorio.setDisciplinaOuProjeto(dto.disciplinaOuProjeto());
        relatorio.setRelatorio(dto.relatorio());
        relatorio.setConsideracoesFinais(dto.consideracoesFinais());
        relatorio.setContatoDaInstituicao(dto.contatoDaInstituicao());

        return repository.save(relatorio);
    }

    private RelatorioAtividade gerarEAnexarPdf(RelatorioAtividade relatorio) {
        try {
            String caminhoArquivoAntigo = relatorio.getCaminhoArquivo();

            byte[] pdfBytes = pdfService.preencherPdf(relatorio);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pdfBytes);
            String hashCalculado = bytesToHex(hashBytes);

            String finalFileName = hashCalculado + ".pdf";
            Path targetLocation = this.fileStorageLocation.resolve(finalFileName);
            Files.write(targetLocation, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            relatorio.setCaminhoArquivo(targetLocation.toString());
            relatorio.setTamanho(formatarTamanho(pdfBytes.length));
            relatorio.setHash(hashCalculado);
            relatorio.setData(new Date());

            RelatorioAtividade relatorioSalva = repository.save(relatorio);

            if (caminhoArquivoAntigo != null && !caminhoArquivoAntigo.isEmpty() && !caminhoArquivoAntigo.equals(targetLocation.toString())) {
                removerDocumentoFisico(caminhoArquivoAntigo);
            }

            return relatorioSalva;

        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar ou salvar arquivo PDF", ex);
        }
    }

    public Resource carregarArquivo(UUID id) {
        RelatorioAtividade relatorio = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatorio Atividade não encontrada"));

        try {
            Path filePath = Paths.get(relatorio.getCaminhoArquivo());
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
