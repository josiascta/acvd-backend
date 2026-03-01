package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.ViagemDTO;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
import br.edu.ifpb.ads.acvd.service.ViagemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/viagens")
@RequiredArgsConstructor
public class ViagemController {

    private final ViagemService viagemService;

    @PostMapping
    public ResponseEntity<ViagemDTO> criarViagem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ViagemDTO dto) throws RegraDeNegocioException {

        UUID servidorId = UUID.fromString(jwt.getSubject());
        ViagemDTO novaViagem = viagemService.criarViagem(servidorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaViagem);
    }

    @GetMapping
    public ResponseEntity<List<ViagemDTO>> listarTodas() {
        return ResponseEntity.ok(viagemService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(viagemService.buscarPorId(id));
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<ViagemDTO>> listarMinhasViagens(@AuthenticationPrincipal Jwt jwt) {
        UUID servidorId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(viagemService.listarViagensDoServidor(servidorId));
    }
}