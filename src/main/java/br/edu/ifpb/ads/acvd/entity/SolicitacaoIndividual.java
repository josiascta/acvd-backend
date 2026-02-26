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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id", unique = true)
    private Viagem viagem;
}