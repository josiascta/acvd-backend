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

        @UniqueConstraint(columnNames = {"discente_id", "viagem_id"})
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
    @JoinColumn(name = "discente_id", nullable = false)
    private User discente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id")
    private ContaBancaria contaBancaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRequisicao status;


    @Column(columnDefinition = "TEXT")
    private String motivoReprovacao;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @Column(precision = 10, scale = 2)
    private BigDecimal inscricaoValor;

}