package br.edu.ifpb.ads.acvd.dto;

import java.util.UUID;

import br.edu.ifpb.ads.acvd.entity.RelatorioAtividade;

public record RelatorioAtividadeDTO(
    UUID id,
    String coordenadoresDaAtividade,
    String disciplinaOuProjeto,
    String relatorio,
    String consideracoesFinais,
    String contatoDaInstituicao,
    UUID viagemId
) {
    public RelatorioAtividadeDTO(RelatorioAtividade entidade) {
        this(
            entidade.getId(),
            entidade.getCoordenadoresDaAtividade(),
            entidade.getDisciplinaOuProjeto(),
            entidade.getRelatorio(),
            entidade.getConsideracoesFinais(),
            entidade.getContatoDaInstituicao(),
            entidade.getViagem() != null ? entidade.getViagem().getId() : null
        );
    }
}
