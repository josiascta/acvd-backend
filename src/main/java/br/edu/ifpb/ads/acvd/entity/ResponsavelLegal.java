package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_responsaveis_legais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponsavelLegal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    private String cpf;

    private String rg;

    private String contato;

    @OneToOne(mappedBy = "responsavelLegal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Documento documentoIdentificacao;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
