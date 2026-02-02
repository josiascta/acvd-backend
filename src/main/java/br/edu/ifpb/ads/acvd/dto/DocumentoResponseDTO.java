package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Documento;
import br.edu.ifpb.ads.acvd.entity.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponseDTO(
        UUID id,
        TipoDocumento tipo,
        String nomeOriginal,
        String tamanho,
        String hash,
        LocalDateTime dataUpload
) {
    public DocumentoResponseDTO(Documento doc) {
        this(doc.getId(), doc.getTipo(), doc.getNomeOriginal(), doc.getTamanho(), doc.getHash(), doc.getDataUpload());
    }
}
