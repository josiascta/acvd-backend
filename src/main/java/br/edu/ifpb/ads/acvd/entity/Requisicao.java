package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_requisicoes", uniqueConstraints = {
        // Regra: Uma requisição por usuário para uma determinada viagem
        @UniqueConstraint(columnNames = {"user_id", "viagem_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User discente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id", nullable = false)
    private ContaBancaria contaBancaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAfastamento afastamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal percentualDiaria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal inscricaoValor;

    @Column(nullable = false)
    private boolean solicitaIncricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRequisicao status;
}