package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    private String nome;

    private String fotoDePerfil;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String matricula;

    @Column(nullable = true)
    private String telefone;

    @Column(nullable = true)
    private String numeroCpf;

    @Column(nullable = true)
    private String numeroRg;

    @Column(nullable = true)
    private String curso;

    @Column(nullable = true)
    private Date dataNascimento;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContaBancaria contaBancaria;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResponsavelLegal responsavelLegal;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Documento documento;

}
