package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Itinerario;
import br.edu.ifpb.ads.acvd.entity.TipoViagem;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ViagemDTO(
        UUID id,
        @NotNull(message = "A data de partida é obrigatória") @FutureOrPresent
        LocalDate dataPartida,
        @NotNull(message = "A data de retorno é obrigatória") @FutureOrPresent
        LocalDate dataRetorno,
        @NotNull @FutureOrPresent
        LocalDate prazoAnexosDiscentes,

        Float valorDiariaCnpq,
        @NotNull
        TipoViagem tipoViagem,
        String nomeResponsavel,
        UUID solicitacaoColetivaId,
        @Valid @NotEmpty(message = "Pelo menos um itinerário deve ser informado")
        List<ItinerarioDTO> itinerarios
) {
    public ViagemDTO(Viagem viagem) {
        this(
                viagem.getId(),
                viagem.getDataPartida(),
                viagem.getDataRetorno(),
                viagem.getPrazoAnexosDiscentes(),
                viagem.getValorDiariaCnpq(),
                viagem.getTipoViagem(),

                viagem.getResponsavel() != null ? viagem.getResponsavel().getNome() : null,
                viagem.getSolicitacaoColetiva() != null ? viagem.getSolicitacaoColetiva().getId() : null,
                viagem.getItinerarios().stream().map(ItinerarioDTO::new).toList()
        );
    }

    public record ItinerarioDTO(
            UUID id,
            @NotNull LocalDateTime horarioEntrada,
            @NotNull LocalDateTime horarioSaida,
            @NotEmpty String local
    ) {
        public ItinerarioDTO(Itinerario itinerario) {
            this(itinerario.getId(), itinerario.getHorarioEntrada(), itinerario.getHorarioSaida(), itinerario.getLocal());
        }
    }
}