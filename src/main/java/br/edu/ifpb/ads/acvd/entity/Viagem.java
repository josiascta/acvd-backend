package br.edu.ifpb.ads.acvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_viagens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate dataPartida;

    @Column(nullable = false)
    private LocalDate dataRetorno;

    @Column(nullable = false)
    private LocalDate prazoAnexosDiscentes;

    @Column(nullable = false)
    private Float valorDiariaCnpq;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoViagem tipoViagem;

    @OneToMany(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itinerario> itinerarios = new ArrayList<>();

    @OneToOne(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private SolicitacaoColetiva solicitacaoColetiva;

    @OneToOne(mappedBy = "viagem") 
    private SolicitacaoIndividual solicitacaoIndividual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private User responsavel;

    public void addItinerario(Itinerario itinerario) {
        itinerarios.add(itinerario);
        itinerario.setViagem(this);
    }
}