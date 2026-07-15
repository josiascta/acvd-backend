package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Requisicao;
import br.edu.ifpb.ads.acvd.entity.StatusRequisicao;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
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
            BigDecimal inscricaoValor,
            TermoResponsabilidadeDTO termoResponsabilidade,
            TipoAfastamento tipoAfastamento,
            Boolean solicitaInscrica
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
                    req.getInscricaoValor(),
                    req.getTermoResponsabilidade() != null ? new TermoResponsabilidadeDTO(req.getTermoResponsabilidade()) : null,
                    req.getTipoAfastamento(),
                    req.getSolicitaInscricao()
            );
        }
    }

    public record AdicionarDiscente(
            @NotBlank(message = "A matrícula do discente é obrigatória")
            String matriculaDiscente,

            TipoAfastamento tipoAfastamento,
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

            TipoAfastamento tipoAfastamento,
            BigDecimal inscricaoValor
    ) {}
}