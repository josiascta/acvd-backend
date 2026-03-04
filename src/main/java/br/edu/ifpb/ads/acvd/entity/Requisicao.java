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
        // Regra: Apenas uma requisição por discente para cada viagem
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

    // Relacionamento com o Discente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discente_id", nullable = false)
    private User discente;

    // Relacionamento com a Viagem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    // Conta bancária pode ser nula inicialmente, pois o discente vai preencher depois
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id")
    private ContaBancaria contaBancaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRequisicao status;

    // Novo campo: Motivo da reprovação (preenchido pelo Servidor)
    @Column(columnDefinition = "TEXT")
    private String motivoReprovacao;

    // Valores financeiros (Podem ser preenchidos pelo servidor ao adicionar ou terem valores padrão)
    @Column(precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @Column(precision = 10, scale = 2)
    private BigDecimal inscricaoValor;

    // NOTA: Os campos do Anexo V e outras submissões do aluno serão adicionados aqui futuramente.
}