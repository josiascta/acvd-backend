package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Documento;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoDTO(UUID id,
                           String caminhoDoArquivo,
                           String tamanho,
                           String hash,
                           String nomeOriginal,
                           LocalDateTime dataUpload) {

    public DocumentoDTO(Documento documento){
        this(
                documento.getId(),
                documento.getCaminhoDoArquivo(),
                documento.getTamanho(),
                documento.getHash(),
                documento.getNomeOriginal(),
                documento.getDataUpload()
        );
    }
}
