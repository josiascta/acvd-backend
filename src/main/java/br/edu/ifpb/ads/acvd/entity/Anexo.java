package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "tb_anexos")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Column(nullable = false)
    private String caminhoArquivo;

    @Column(nullable = false)
    private String tamanho;

    // Novo campo adicionado para manter a mesma lógica de hash do Documento
    @Column(nullable = false)
    private String hash;
}