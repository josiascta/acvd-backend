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
    
    @OneToOne(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private SolicitacaoIndividual solicitacaoIndividual;
    
<<<<<<< HEAD
    @OneToOne(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlanejamentoAtividade planejamentoAtividade;

    @OneToOne(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private RelatorioAtividade relatorioAtividade;
=======
    @OneToOne(mappedBy = "planejamentoAtividade", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlanejamentoAtividade planejamentoAtividade;
>>>>>>> 5ce99292ffcc0e2c18175a35df9649f3ce0cb94f

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private User responsavel;

    public void addItinerario(Itinerario itinerario) {
        itinerarios.add(itinerario);
        itinerario.setViagem(this);
    }
}