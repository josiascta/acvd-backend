package br.edu.ifpb.ads.acvd.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "relatorios_viagem_discente")
public class RelatorioViagemDiscente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vinculado à solicitação do aluno (onde já temos nome, CPF, curso, etc.)
    @OneToOne
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private SolicitacaoIndividual solicitacao;

    @Column(columnDefinition = "TEXT")
    private String descricaoAtividades; // Campo: Descrição sucinta da viagem [cite: 39, 42]

    private BigDecimal valorAjudaCusto; // Campo: Valor da ajuda de custo [cite: 53]
    private String ajudaCustoExtenso;   // Valor por extenso [cite: 53]

    private BigDecimal valorPassagens;  // Campo: Valor das passagens 
    private String passagensExtenso;    // Valor por extenso 
    private String numeroBilhetes;      // Campo: Bilhete nº 

    @Column(columnDefinition = "TEXT")
    private String observacoes;         // Campo: Observações [cite: 49]

    private LocalDate dataRelatorio;    // Cidade-UF, data [cite: 51]

    // Getters e Setters...
}