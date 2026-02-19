package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.ResponsavelLegalDTO;
import br.edu.ifpb.ads.acvd.service.ResponsavelLegalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users/me/responsavel-legal")
@RequiredArgsConstructor
public class ResponsavelLegalController {

    private final ResponsavelLegalService responsavelLegalService;

    @GetMapping
    public ResponseEntity<ResponsavelLegalDTO> obter(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(responsavelLegalService.obter(userId));
    }

    @PutMapping
    public ResponseEntity<ResponsavelLegalDTO> salvarDados(@AuthenticationPrincipal Jwt jwt,
                                                           @RequestBody ResponsavelLegalDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(responsavelLegalService.atualizarDados(userId, dto));
    }

    @PostMapping(value = "/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadDocumento(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam("file") MultipartFile file) {
        UUID userId = UUID.fromString(jwt.getSubject());
        responsavelLegalService.uploadDocumentoIdentificacao(userId, file);
        return ResponseEntity.noContent().build();
    }
}