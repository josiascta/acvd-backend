package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.Documento;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
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

    /**
     * Processa o upload físico e retorna uma instância de Documento pronta para ser vinculada.
     */
    @Transactional
    public Documento processarUpload(MultipartFile file) {
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

            String finalFileName = hashCalculado + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(finalFileName);
            Files.move(tempFile, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Documento documento = new Documento();
            documento.setCaminhoDoArquivo(targetLocation.toString());
            documento.setHash(hashCalculado);
            documento.setTamanho(formatarTamanho(file.getSize()));
            documento.setNomeOriginal(originalName);
            documento.setDataUpload(LocalDateTime.now());

            return documento;

        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar upload", ex);
        }
    }

    @Transactional
    public Documento salvarDocumentoDoUsuario(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (user.getDocumento() != null) {
            removerDocumentoFisico(user.getDocumento());
            documentoRepository.delete(user.getDocumento());
        }

        Documento documento = processarUpload(file);
        documento.setUser(user);
        user.setDocumento(documento);

        return documentoRepository.save(documento);
    }

    public Resource carregarArquivo(Documento documento) {
        try {
            Path filePath = Paths.get(documento.getCaminhoDoArquivo());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado");
            }
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro no caminho do arquivo");
        }
    }

    public void removerDocumentoFisico(Documento documento) {
        if (documento == null) return;
        try {
            Files.deleteIfExists(Paths.get(documento.getCaminhoDoArquivo()));
        } catch (IOException e) {
            System.err.println("Erro ao deletar arquivo: " + e.getMessage());
        }
    }

    public Documento buscarDocumentoDoUsuario(UUID userId) {
        return documentoRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Documento buscarPorId(UUID docId, UUID userId) {
        Documento doc = documentoRepository.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Validação de Segurança: O documento pertence ao usuário ou ao seu responsável?
        boolean isOwner = doc.getUser() != null && doc.getUser().getUserId().equals(userId);
        boolean isResponsavelOwner = doc.getResponsavelLegal() != null &&
                doc.getResponsavelLegal().getUser().getUserId().equals(userId);

        if (!isOwner && !isResponsavelOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return doc;
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