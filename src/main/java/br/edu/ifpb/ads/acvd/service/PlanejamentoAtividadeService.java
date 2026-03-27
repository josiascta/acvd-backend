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

import br.edu.ifpb.ads.acvd.dto.PlanejamentoAtividadeDTO;
import br.edu.ifpb.ads.acvd.entity.PlanejamentoAtividade;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.PlanejamentoAtividadeRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;

@Service
public class PlanejamentoAtividadeService {

    private final PlanejamentoAtividadeRepository repository;
    private final ViagemRepository viagemRepository;
    private final Path fileStorageLocation;
    private final PdfPlanejamentoAtividade pdfService;

        public PlanejamentoAtividadeService(PlanejamentoAtividadeRepository repository,
                                      ViagemRepository viagemRepository,
                                      PdfPlanejamentoAtividade pdfService,
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
    public PlanejamentoAtividadeDTO processarPlanejamento(UUID userId, PlanejamentoAtividadeDTO dto) {
        PlanejamentoAtividade entidadeSalva = salvarDadosNoBanco(userId, dto);
        PlanejamentoAtividade entidadeComPdf = gerarEAnexarPdf(entidadeSalva);
        return new PlanejamentoAtividadeDTO(entidadeComPdf);
    }

    
    private PlanejamentoAtividade salvarDadosNoBanco(UUID userId, PlanejamentoAtividadeDTO dto) {
        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        PlanejamentoAtividade planejamento = repository.findByViagemId(viagem.getId())
                .orElse(new PlanejamentoAtividade());

        planejamento.setViagem(viagem);

        if (planejamento.getData() == null) {
            planejamento.setData(new Date());
            planejamento.setCaminhoArquivo("");
            planejamento.setTamanho("");
            planejamento.setHash("");
        }

        planejamento.setCoordenadoresDaAtividade(dto.coordenadoresDaAtividade());
        planejamento.setCoordenadoresDePesquisaExtensao(dto.coordenadoresDePesquisaExtensao());
        planejamento.setDisciplina(dto.disciplina());
        planejamento.setCurso(dto.curso());
        planejamento.setTurma(dto.turma());
        planejamento.setMetodologia(dto.metodologia());
        planejamento.setObjetivos(dto.objetivos());
        planejamento.setCargaHorariaCompatibilidade(dto.cargaHorariaCompatibilidade());
        planejamento.setJustificativaImportancia(dto.justificativaImportancia());
        planejamento.setNumeroParticipantes(Integer.parseInt(dto.numeroParticipantes()));
        planejamento.setItensSeguranca(dto.itensSeguranca());
        planejamento.setCargaHorariaNoDiarioDeClasse(dto.cargaHorariaNoDiarioDeClasse());
        planejamento.setContatoDosCoordenadores(dto.contatoDosCoordenadores());

        planejamento.setViagem(viagem);

        return repository.save(planejamento);
    }

    private PlanejamentoAtividade gerarEAnexarPdf(PlanejamentoAtividade planejamento) {
        try {
            String caminhoArquivoAntigo = planejamento.getCaminhoArquivo();

            byte[] pdfBytes = pdfService.preencherPdf(planejamento);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pdfBytes);
            String hashCalculado = bytesToHex(hashBytes);

            String finalFileName = hashCalculado + ".pdf";
            Path targetLocation = this.fileStorageLocation.resolve(finalFileName);
            Files.write(targetLocation, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            planejamento.setCaminhoArquivo(targetLocation.toString());
            planejamento.setTamanho(formatarTamanho(pdfBytes.length));
            planejamento.setHash(hashCalculado);
            planejamento.setData(new Date());

            PlanejamentoAtividade planejamentoSalva = repository.save(planejamento);

            if (caminhoArquivoAntigo != null && !caminhoArquivoAntigo.isEmpty() && !caminhoArquivoAntigo.equals(targetLocation.toString())) {
                removerDocumentoFisico(caminhoArquivoAntigo);
            }

            return planejamentoSalva;

        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar ou salvar arquivo PDF", ex);
        }
    }

    public Resource carregarArquivo(UUID id) {
        PlanejamentoAtividade planejamento = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação Coletiva não encontrada"));

        try {
            Path filePath = Paths.get(planejamento.getCaminhoArquivo());
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
