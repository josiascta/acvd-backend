package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Requisicao;
import br.edu.ifpb.ads.acvd.entity.StatusRequisicao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class RequisicaoDTO {

    public record Response(
            UUID id,
            UUID viagemId,
            UUID discenteId,
            String discenteNome,
            String discenteMatricula,
            StatusRequisicao status,
            String motivoReprovacao,
            BigDecimal valorDiaria,
            BigDecimal inscricaoValor
    ) {
        public Response(Requisicao req) {
            this(
                    req.getId(),
                    req.getViagem().getId(),
                    req.getDiscente().getUserId(),
                    req.getDiscente().getNome(),
                    req.getDiscente().getMatricula(),
                    req.getStatus(),
                    req.getMotivoReprovacao(),
                    req.getValorDiaria(),
                    req.getInscricaoValor()
            );
        }
    }

    public record AdicionarDiscente(
            @NotBlank(message = "A matrícula do discente é obrigatória")
            String matriculaDiscente,

            BigDecimal valorDiaria,
            BigDecimal inscricaoValor
    ) {}

    public record Avaliar(
            @NotNull(message = "O estado da avaliação é obrigatório")
            StatusRequisicao status,
            String motivoReprovacao
    ) {}

    public record AdicionarDiscentePorEmail(
            @NotBlank(message = "O e-mail do discente é obrigatório")
            @Email(message = "Formato de e-mail inválido")
            String emailDiscente,

            BigDecimal valorDiaria,
            BigDecimal inscricaoValor
    ) {}
}