package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record RequisicaoCreateDTO(
        @NotNull(message = "O ID da Viagem é obrigatório") UUID viagemId,
        @NotNull(message = "O tipo de afastamento é obrigatório") TipoAfastamento afastamento,
        @NotNull @PositiveOrZero BigDecimal percentualDiaria,
        @NotNull @PositiveOrZero BigDecimal valorDiaria,
        @NotNull @PositiveOrZero BigDecimal inscricaoValor,
        @NotNull boolean solicitaIncricao
) {}