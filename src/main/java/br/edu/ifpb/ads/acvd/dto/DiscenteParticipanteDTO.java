package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.ContaBancaria;
import br.edu.ifpb.ads.acvd.entity.User;

public record DiscenteParticipanteDTO(String nome,
                                      String matricula,
                                      String cpf,
                                      String banco,
                                      String agencia,
                                      String op,
                                      String conta) {
    public DiscenteParticipanteDTO(User discente, ContaBancaria contaBancaria) {
        this(
                discente.getNome() != null ? discente.getNome() : "",
                discente.getMatricula() != null ? discente.getMatricula() : "",
                discente.getNumeroCpf() != null ? discente.getNumeroCpf() : "",
                contaBancaria != null && contaBancaria.getBanco() != null ? contaBancaria.getBanco() : "",
                contaBancaria != null && contaBancaria.getAgencia() != null ? contaBancaria.getAgencia() : "",
                contaBancaria != null && contaBancaria.getOperacao() != null ? contaBancaria.getOperacao() : "",
                contaBancaria != null && contaBancaria.getNumero() != null ? contaBancaria.getNumero() : ""
        );
    }
}
