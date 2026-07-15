package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.TermoResponsabilidade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class TermoResponsabilidadeService {

    private final Path fileStorageLocation;

    public TermoResponsabilidadeService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    public TermoResponsabilidade processarUpload(MultipartFile file, String prefixo) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = file.getBytes();
        byte[] hashBytes = digest.digest(fileBytes);
        String hashCalculado = bytesToHex(hashBytes);

        String extensao = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                : ".pdf";

        String nomeArquivoFisico = prefixo + "_" + hashCalculado.substring(0, 8) + extensao;
        Path targetLocation = this.fileStorageLocation.resolve(nomeArquivoFisico);

        Files.write(targetLocation, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        TermoResponsabilidade termo = new TermoResponsabilidade();
        termo.setCaminhoArquivo(targetLocation.toString());
        termo.setHash(hashCalculado);
        termo.setTamanho(formatarTamanho(file.getSize()));
        termo.setData(new Date());

        return termo;
    }

    public void removerArquivoFisico(TermoResponsabilidade termo) {
        if (termo != null && termo.getCaminhoArquivo() != null) {
            try {
                Files.deleteIfExists(Paths.get(termo.getCaminhoArquivo()));
            } catch (Exception e) {
                System.err.println("Erro ao deletar arquivo do termo: " + e.getMessage());
            }
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

    public Resource carregarArquivo(TermoResponsabilidade termo) {
        try {
            Path filePath = Paths.get(termo.getCaminhoArquivo());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O ficheiro físico não foi encontrado no servidor.");
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao tentar ler o ficheiro.", ex);
        }
    }
}