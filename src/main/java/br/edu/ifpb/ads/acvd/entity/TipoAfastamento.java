package br.edu.ifpb.ads.acvd.entity;

import lombok.Getter;

@Getter
public enum TipoAfastamento {

    MENOR_04_HORAS("Menor que 04 horas"),
    MAIOR_04_HORAS("Maior que 04 horas"),
    MAIOR_08_HORAS("Maior que 08 horas"),
    MAIOR_08_HORAS_ALIMENTACAO_OU_LOCOMOCAO("Maior que 08 horas - Alimentação/Locomoção"),
    MAIOR_08_HORAS_ALIMENTACAO_E_LOCOMOCAO("Maior que 08 horas - Alimentação e Locomoção"),
    MAIOR_08_HORAS_ALIMENTACAO_E_HOSPEDAGEM("Maior que 08 horas - Alimentação e Hospedagem"),
    MAIOR_08_HORAS_ALIMENTACAO_E_HOSPEDAGEM_E_LOCOMOCAO("Maior que 08 horas - Alimentação, Hospedagem e Locomoção");

    private final String descricao;

    TipoAfastamento(String descricao) {
        this.descricao = descricao;
    }
}
