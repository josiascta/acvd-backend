package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Documento;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponseDTO(
        UUID id,
        String nomeOriginal,
        String tamanho,
        String hash,
        LocalDateTime dataUpload
) {
    public DocumentoResponseDTO(Documento doc) {
        this(doc.getId(), doc.getNomeOriginal(), doc.getTamanho(), doc.getHash(), doc.getDataUpload());
    }
}
