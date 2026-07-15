package br.edu.ifpb.ads.acvd.entity;

import lombok.Getter;

@Getter
public enum TipoAtividade {

    PROJETO("Projeto"),
    DISCIPLINA("Disciplina");

    private final String descricao;

    TipoAtividade(String descricao) {
        this.descricao = descricao;
    }
}
