package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_contas_bancarias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String banco;

    private String numero;

    private String agencia;

    private String operacao;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
