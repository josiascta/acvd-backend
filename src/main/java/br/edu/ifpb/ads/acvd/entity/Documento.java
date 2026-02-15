package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_legal_id", unique = true)
    private ResponsavelLegal responsavelLegal;
}