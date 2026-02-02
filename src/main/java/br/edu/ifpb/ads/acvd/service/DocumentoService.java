package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.Documento;
import br.edu.ifpb.ads.acvd.entity.TipoDocumento;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.DocumentoRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final UserRepository userRepository;
    private final Path fileStorageLocation;

    public DocumentoService(DocumentoRepository documentoRepository,
                            UserRepository userRepository,
                            @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.documentoRepository = documentoRepository;
        this.userRepository = userRepository;

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    @Transactional
    public Documento salvarDocumento(UUID userId, MultipartFile file, TipoDocumento tipo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String originalName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalName != null && originalName.contains(".")) {
            fileExtension = originalName.substring(originalName.lastIndexOf("."));
        }

        Path tempFile;
        String hashCalculado;

        try {
            tempFile = Files.createTempFile(this.fileStorageLocation, "upload_", ".tmp");

            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(tempFile, StandardOpenOption.WRITE)) {

                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
                    digestInputStream.transferTo(outputStream);
                }

                hashCalculado = bytesToHex(digest.digest());
            }

        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar upload", ex);
        }


        String finalFileName = hashCalculado + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(finalFileName);

        try {
            Files.move(tempFile, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar arquivo final", ex);
        }

        String tamanhoFormatado = formatarTamanho(file.getSize());

        Documento documento = new Documento();
        documento.setTipo(tipo);
        documento.setCaminhoDoArquivo(targetLocation.toString());
        documento.setHash(hashCalculado);
        documento.setTamanho(tamanhoFormatado);
        documento.setNomeOriginal(originalName);
        documento.setDataUpload(LocalDateTime.now());
        documento.setUser(user);

        return documentoRepository.save(documento);
    }

    public List<Documento> listarMeusDocumentos(UUID userId) {
        return documentoRepository.findByUserUserId(userId);
    }

    public Documento buscarDocumento(UUID docId, UUID userId) {
        Documento doc = documentoRepository.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado"));

        if (!doc.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este documento");
        }
        return doc;
    }

    public Resource carregarArquivo(Documento documento) {
        try {
            Path filePath = Paths.get(documento.getCaminhoDoArquivo());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo físico não encontrado: " + documento.getNomeOriginal());
            }
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no caminho do arquivo", ex);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
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
