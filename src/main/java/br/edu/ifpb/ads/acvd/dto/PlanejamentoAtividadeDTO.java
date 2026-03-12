package br.edu.ifpb.ads.acvd.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import br.edu.ifpb.ads.acvd.entity.PlanejamentoAtividade;

public record PlanejamentoAtividadeDTO(
    UUID id,
    @NotNull(message = "O ID da Viagem é obrigatório") UUID viagemId,
    String coordenadoresDaAtividade,
    String coordenadoresDePesquisaExtensao,
    String disciplina,
    String curso,
    String turma,
    String metodologia,
    String objetivos,
    String cargaHorariaCompatibilidade,
    String justificativaImportancia,
    String numeroParticipantes,
    String itensSeguranca,
    String cargaHorariaNoDiarioDeClasse,
    String contatoDosCoordenadores
) {
    public PlanejamentoAtividadeDTO(PlanejamentoAtividade entidade){
        this(
        entidade.getId(), 
        entidade.getViagem() != null ? entidade.getViagem().getId() : null,
        entidade.getCoordenadoresDaAtividade(), 
        entidade.getCoordenadoresDePesquisaExtensao(),
        entidade.getDisciplina(),
        entidade.getCurso(),
        entidade.getTurma(),
        entidade.getMetodologia(),
        entidade.getObjetivos(), 
        entidade.getCargaHorariaCompatibilidade(), 
        entidade.getJustificativaImportancia(), 
        entidade.getNumeroParticipantes().toString(),
        entidade.getItensSeguranca(),
        entidade.getCargaHorariaNoDiarioDeClasse(),
        entidade.getContatoDosCoordenadores());
    }
}
