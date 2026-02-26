package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_solicitacoes_coletivas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoColetiva extends Anexo {

    @Column(nullable = false)
    private String solicitanteNome;

    @Column(nullable = false)
    private String solicitanteMatricula;

    @Column(nullable = false)
    private String solicitanteTelefone;

    @Column(nullable = false)
    private String solicitanteEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAfastamento afastamento;

    @Column(nullable = false)
    private boolean inscricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtividade disciplinaOuProjeto;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(nullable = false)
    private String setorDepartamentoCurso;

    // Associação com Viagem (lado proprietário da relação)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", unique = true)
    private Viagem viagem;
}