package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoColetiva;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
import br.edu.ifpb.ads.acvd.entity.TipoAtividade;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.UUID;

public record SolicitacaoColetivaDTO(
        UUID id,
        @NotNull(message = "A data do anexo é obrigatória") Date data,
        String caminhoArquivo, // Geralmente preenchido pelo backend após o upload
        String tamanho,

        @NotBlank(message = "O nome do solicitante é obrigatório") String solicitanteNome,
        @NotBlank(message = "A matrícula é obrigatória") String solicitanteMatricula,
        @NotBlank(message = "O telefone é obrigatório") String solicitanteTelefone,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String solicitanteEmail,

        @NotNull(message = "O tipo de afastamento é obrigatório") TipoAfastamento afastamento,
        boolean inscricao,
        @NotNull(message = "O tipo de atividade é obrigatório") TipoAtividade disciplinaOuProjeto,
        @NotBlank(message = "A justificativa é obrigatória") String justificativa,
        @NotBlank(message = "O setor/departamento/curso é obrigatório") String setorDepartamentoCurso
) {
    public SolicitacaoColetivaDTO(SolicitacaoColetiva entidade) {
        this(
                entidade.getId(),
                entidade.getData(),
                entidade.getCaminhoArquivo(),
                entidade.getTamanho(),
                entidade.getSolicitanteNome(),
                entidade.getSolicitanteMatricula(),
                entidade.getSolicitanteTelefone(),
                entidade.getSolicitanteEmail(),
                entidade.getAfastamento(),
                entidade.isInscricao(),
                entidade.getDisciplinaOuProjeto(),
                entidade.getJustificativa(),
                entidade.getSetorDepartamentoCurso()
        );
    }
}