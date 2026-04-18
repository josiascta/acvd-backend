package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoColetiva;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
import br.edu.ifpb.ads.acvd.entity.TipoAtividade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.util.Date;
import java.util.UUID;

public record SolicitacaoColetivaDTO(
        UUID id,
        @NotNull(message = "O ID da Viagem é obrigatório") UUID viagemId,


        @NotNull(message = "A data da solicitação é obrigatória") @PastOrPresent Date solicitadoEm,
        @NotNull(message = "O tipo de afastamento é obrigatório") TipoAfastamento afastamento,

        boolean inscricao,
        boolean hospedagem,
        boolean locomocaoUrbana,
        boolean alimentacao,
        boolean passagem,

        boolean planejamentoVisitaTecnica,
        boolean planilha,
        boolean termoResponsabilidade,
        boolean outrosDocumentos,

        @NotNull(message = "O tipo de atividade é obrigatório") TipoAtividade disciplinaOuProjeto,
        @NotBlank(message = "A justificativa é obrigatória") String justificativa,
        @NotBlank(message = "A coordenação (setor/departamento/curso) é obrigatória") String setorDepartamentoCurso
) {
    public SolicitacaoColetivaDTO(SolicitacaoColetiva entidade) {
        this(
                entidade.getId(),
                entidade.getViagem() != null ? entidade.getViagem().getId() : null,

                entidade.getSolicitadoEm(),
                entidade.getAfastamento(),
                entidade.isInscricao(),
                entidade.isHospedagem(),
                entidade.isLocomocaoUrbana(),
                entidade.isAlimentacao(),
                entidade.isPassagem(),
                entidade.isPlanejamentoVisitaTecnica(),
                entidade.isPlanilha(),
                entidade.isTermoResponsabilidade(),
                entidade.isOutrosDocumentos(),
                entidade.getDisciplinaOuProjeto(),
                entidade.getJustificativa(),
                entidade.getSetorDepartamentoCurso()
        );
    }
}