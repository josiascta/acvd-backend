package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.util.Date;
import java.util.UUID;

public record SolicitacaoIndividualDTO(
        UUID id,
        @NotNull(message = "A data do anexo é obrigatória") Date data,
        String caminhoArquivo,
        String tamanho,

        @NotBlank(message = "A justificativa é obrigatória") String justificativa,
        @NotNull(message = "A data da solicitação é obrigatória") @PastOrPresent Date solicitadoEm,
        @NotNull(message = "O tipo de afastamento é obrigatório") TipoAfastamento afastamento
) {
    public SolicitacaoIndividualDTO(SolicitacaoIndividual entidade) {
        this(
                entidade.getId(),
                entidade.getData(),
                entidade.getCaminhoArquivo(),
                entidade.getTamanho(),
                entidade.getJustificativa(),
                entidade.getSolicitadoEm(),
                entidade.getAfastamento()
        );
    }
}