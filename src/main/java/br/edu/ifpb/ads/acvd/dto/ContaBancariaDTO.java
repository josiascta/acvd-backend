package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.ContaBancaria;

import java.util.UUID;

public record ContaBancariaDTO(UUID id,
                               String banco,
                               String numero,
                               String agencia,
                               String operacao) {

    public ContaBancariaDTO(ContaBancaria contaBancaria){
        this(contaBancaria.getId(),
                contaBancaria.getBanco(),
                contaBancaria.getNumero(),
                contaBancaria.getAgencia(),
                contaBancaria.getOperacao());
    }
}
