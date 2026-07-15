package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.ResponsavelLegal;

import java.util.UUID;

public record ResponsavelLegalDTO(UUID id,
                                  String nome,
                                  String cpf,
                                  String rg,
                                  String contato,
                                  DocumentoDTO documento) {

    public ResponsavelLegalDTO(ResponsavelLegal responsavelLegal){
        this(responsavelLegal.getId(),
                responsavelLegal.getNome(),
                responsavelLegal.getCpf(),
                responsavelLegal.getRg(),
                responsavelLegal.getContato(),
                responsavelLegal.getDocumentoIdentificacao() != null ? new DocumentoDTO(responsavelLegal.getDocumentoIdentificacao()) : null);
    }
}
