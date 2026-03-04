package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Requisicao;
import br.edu.ifpb.ads.acvd.entity.StatusRequisicao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class RequisicaoDTO {

    // 1. DTO de Resposta (O que vai para o Frontend)
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

    // 2. DTO para o Servidor adicionar um Discente à viagem
    public record AdicionarDiscente(
            @NotBlank(message = "A matrícula do discente é obrigatória")
            String matriculaDiscente,

            // Opcional: O servidor já pode definir os valores que o aluno vai receber
            BigDecimal valorDiaria,
            BigDecimal inscricaoValor
    ) {}

    // 3. DTO para o Servidor Avaliar (Aprovar/Reprovar)
    public record Avaliar(
            @NotNull(message = "O estado da avaliação é obrigatório")
            StatusRequisicao status,

            // Será validado como obrigatório no Service se o status for REPROVADO
            String motivoReprovacao
    ) {}

    public record AdicionarDiscentePorEmail(
            @NotBlank(message = "O e-mail do discente é obrigatório")
            @jakarta.validation.constraints.Email(message = "Formato de e-mail inválido")
            String emailDiscente,

            BigDecimal valorDiaria,
            BigDecimal inscricaoValor
    ) {}
}