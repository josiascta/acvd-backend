package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.util.Date;
import java.util.UUID;

public record SolicitacaoIndividualDTO(
        UUID id,
        @NotNull(message = "O ID da Viagem é obrigatório") UUID viagemId,
        Date data,
        String caminhoArquivo,
        String tamanho,
        String hash,

        @NotBlank(message = "A justificativa é obrigatória") String justificativa,
        @NotNull(message = "A data da solicitação é obrigatória") @PastOrPresent Date solicitadoEm,
        @NotNull(message = "O tipo de afastamento é obrigatório") TipoAfastamento afastamento,

        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "A matrícula é obrigatória") String matricula,
        String curso,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email,
        String telefone
) {
    public SolicitacaoIndividualDTO(SolicitacaoIndividual entidade) {
        this(
                entidade.getId(),
                entidade.getViagem() != null ? entidade.getViagem().getId() : null,
                entidade.getData(),
                entidade.getCaminhoArquivo(),
                entidade.getTamanho(),
                entidade.getHash(),
                entidade.getJustificativa(),
                entidade.getSolicitadoEm(),
                entidade.getAfastamento(),
                entidade.getNome(),
                entidade.getMatricula(),
                entidade.getCurso(),
                entidade.getEmail(),
                entidade.getTelefone()
        );
    }
}