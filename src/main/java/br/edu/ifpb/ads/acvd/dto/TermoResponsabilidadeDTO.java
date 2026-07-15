package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.TermoResponsabilidade;

import java.util.Date;
import java.util.UUID;

public record TermoResponsabilidadeDTO(
        UUID id,
        String tamanho,
        String hash,
        Date data
) {
    public TermoResponsabilidadeDTO(TermoResponsabilidade termo) {
        this(termo.getId(), termo.getTamanho(), termo.getHash(), termo.getData());
    }
}