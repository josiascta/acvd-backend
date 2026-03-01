package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Requisicao;
import br.edu.ifpb.ads.acvd.entity.StatusRequisicao;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;

import java.math.BigDecimal;
import java.util.UUID;

public record RequisicaoDTO(
        UUID id,
        UUID discenteId,
        UUID viagemId,
        UUID contaBancariaId,
        TipoAfastamento afastamento,
        BigDecimal percentualDiaria,
        BigDecimal valorDiaria,
        BigDecimal inscricaoValor,
        boolean solicitaIncricao,
        StatusRequisicao status
) {
    public RequisicaoDTO(Requisicao req) {
        this(
                req.getId(),
                req.getDiscente().getUserId(),
                req.getViagem().getId(),
                req.getContaBancaria().getId(),
                req.getAfastamento(),
                req.getPercentualDiaria(),
                req.getValorDiaria(),
                req.getInscricaoValor(),
                req.isSolicitaIncricao(),
                req.getStatus()
        );
    }
}
