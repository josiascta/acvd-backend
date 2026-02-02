package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    @Column(nullable = false)
    private String caminhoDoArquivo;

    @Column(nullable = false)
    private String tamanho;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    private String nomeOriginal;

    @Column(nullable = false)
    private LocalDateTime dataUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
