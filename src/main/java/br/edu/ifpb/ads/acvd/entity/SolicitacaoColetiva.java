package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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

    @Column(nullable = true)
    private String solicitanteTelefone;

    @Column(nullable = false)
    private String solicitanteEmail;

    @Column(nullable = true)
    private String curso;

    @Column(nullable = false)
    private Date solicitadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAfastamento afastamento;

    @Column(nullable = false)
    private boolean inscricao;

    @Column(nullable = false)
    private boolean hospedagem;

    @Column(nullable = false)
    private boolean locomocaoUrbana;

    @Column(nullable = false)
    private boolean alimentacao;

    @Column(nullable = false)
    private boolean passagem;

    @Column(nullable = false)
    private boolean planejamentoVisitaTecnica;

    @Column(nullable = false)
    private boolean planilha;

    @Column(nullable = false)
    private boolean termoResponsabilidade;

    @Column(nullable = false)
    private boolean outrosDocumentos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtividade disciplinaOuProjeto;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(nullable = false)
    private String setorDepartamentoCurso; // Representa o campoCoord no PDF

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", unique = true)
    private Viagem viagem;
}