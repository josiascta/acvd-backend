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
@Table(name = "tb_relatorio_atividade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioAtividade extends Anexo {
   
    @Column(nullable = false)
    private String coordenadoresDaAtividade;
    
    @Column(nullable = false)
    private String disciplinaOuProjeto;
    
    @Column(nullable = false, length = 1000)
    private String relatorio;
    
    @Column(nullable = false)
    private String consideracoesFinais;
    
    @Column(nullable = false)
    private String contatoDaInstituicao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", unique = true)
    private Viagem viagem;
}
