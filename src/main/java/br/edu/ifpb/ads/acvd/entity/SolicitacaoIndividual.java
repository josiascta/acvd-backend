package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "tb_solicitacoes_individuais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoIndividual extends Anexo {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String justificativa;

    @Column(nullable = false)
    private Date solicitadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAfastamento afastamento;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String matricula;

    @Column(nullable = false)
    private String cpf; // Novo 

    @Column(nullable = true)
    private String curso;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String telefone;

    @Column(columnDefinition = "TEXT")
    private String endereco; // Novo 

    // --- DADOS BANCÁRIOS ---
    private String banco; // Novo 
    private String agencia; // Novo 
    private String conta; // Novo 

    // --- AUXÍLIOS SOLICITADOS ---
    private boolean solicitaInscricao; // Novo [cite: 8]
    private boolean solicitaPassagem; // Novo [cite: 8]
    private boolean solicitaHospedagem; // Novo [cite: 15]
    private boolean solicitaLocomocao; // Novo [cite: 17]
    private boolean solicitaAlimentacao; // Novo [cite: 18]

    // --- PERÍODO DE AFASTAMENTO ---
    private String dataSaida; // Novo [cite: 11]
    private String horaSaida; // Novo [cite: 16]
    private String dataChegada; // Novo [cite: 14]
    private String horaChegada; // Novo [cite: 19]

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", unique = true)
    private Viagem viagem;
    @Column(nullable = true)
    private String campus; // Ex: "Campus Campina Grande" [cite: 16]

    @Column(nullable = true)
    private String turmaPeriodo; // Ex: "P3 / Noite" [cite: 11]

    @Column(columnDefinition = "TEXT")
    private String atividadeEvento; // Descrição da atividade/evento [cite: 20, 25]

    @Column(nullable = true)
    private String localidadeEvento; // Cidade/Região onde ocorrerá [cite: 27]

    // --- DADOS PARA O TERMO DE CIÊNCIA (PARA MAIORES DE IDADE) ---
    // O PDF exige assinatura e contato de um familiar [cite: 38, 39, 40]
    
    @Column(nullable = true)
    private String nomeFamiliar; 

    @Column(nullable = true)
    private String contatoFamiliar;

    // --- CAMINHO PARA O SEGUNDO PDF ---
    // Como você terá dois arquivos (Anexo II e Anexo V), 
    // precisamos de um campo extra para o caminho do Termo
    @Column(nullable = true)
    private String caminhoArquivoTermo;
}