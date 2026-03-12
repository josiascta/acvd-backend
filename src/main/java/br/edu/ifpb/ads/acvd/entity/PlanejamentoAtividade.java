package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_planejamento_atividade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanejamentoAtividade extends Anexo{
    
    @Column(nullable = false)
    private String coordenadoresDaAtividade;
    
    @Column(nullable = false)
    private String coordenadoresDePesquisaExtensao;
    
    @Column(nullable = false)
    private String disciplina;
    
    @Column(nullable = false)
    private String curso;
    
    @Column(nullable = false)
    private String turma;
    
    @Column(nullable = false)
    private String metodologia;
    
    @Column(nullable = false)
    private String objetivos;
    
    @Column(nullable = false)
    private String cargaHorariaCompatibilidade;
    
    @Column(nullable = false)
    private String justificativaImportancia;
    
    @Column(nullable = false)
    private Integer numeroParticipantes;
    
    @Column(nullable = false)
    private String itensSeguranca;
    
    @Column(nullable = false)
    private String cargaHorariaNoDiarioDeClasse;
    
    @Column(nullable = false)
    private String contatoDosCoordenadores;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", unique = true)
    private Viagem viagem;
}
