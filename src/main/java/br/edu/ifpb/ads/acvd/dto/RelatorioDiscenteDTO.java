package br.edu.ifpb.ads.acvd.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Troque 'class' por 'record' e use parênteses
public record RelatorioDiscenteDTO(
    UUID solicitacaoId,
    String descricaoAtividades,
    BigDecimal valorAjudaCusto,
    String ajudaCustoExtenso,
    BigDecimal valorPassagens,
    String passagensExtenso,
    String numeroBilhetes,
    String observacoes
) {}