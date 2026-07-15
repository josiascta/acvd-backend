package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.DocumentoResponseDTO;
import br.edu.ifpb.ads.acvd.entity.Documento;
import br.edu.ifpb.ads.acvd.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PreAuthorize("hasRole('DISCENTE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponseDTO> uploadDocumento(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) {

        UUID userId = UUID.fromString(jwt.getSubject());
        Documento documento = documentoService.salvarDocumentoDoUsuario(userId, file);

        return ResponseEntity.ok(new DocumentoResponseDTO(documento));
    }

    @PreAuthorize("hasAnyRole('DISCENTE', 'SERVIDOR')")
    @GetMapping
    public ResponseEntity<DocumentoResponseDTO> getMeuDocumento(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Documento doc = documentoService.buscarDocumentoDoUsuario(userId);
        return ResponseEntity.ok(new DocumentoResponseDTO(doc));
    }

    @PreAuthorize("hasAnyRole('DISCENTE', 'SERVIDOR')")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocumento(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = UUID.fromString(jwt.getSubject());

        Documento doc = documentoService.buscarPorId(id, userId);
        Resource resource = documentoService.carregarArquivo(doc);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(Paths.get(doc.getCaminhoDoArquivo()));
        } catch (IOException ex) {}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNomeOriginal() + "\"")
                .body(resource);
    }
}