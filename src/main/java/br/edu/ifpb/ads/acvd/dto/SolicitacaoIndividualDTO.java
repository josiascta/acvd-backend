package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.util.Date;
import java.util.UUID;

public record SolicitacaoIndividualDTO(
        UUID id,
        @NotNull(message = "O ID da Viagem é obrigatório") UUID viagemId,
        Date data,
        String caminhoArquivo,
        String caminhoArquivoTermo, 
        String tamanho,
        String hash,

        @NotBlank(message = "A justificativa é obrigatória") String justificativa,
        @NotNull(message = "A data da solicitação é obrigatória") @PastOrPresent Date solicitadoEm,
        @NotNull(message = "O tipo de afastamento é obrigatório") TipoAfastamento afastamento,

        // Dados do Solicitante
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O CPF é obrigatório") String cpf,
        @NotBlank(message = "A matrícula é obrigatória") String matricula,
        String curso,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email,
        String telefone,
        String endereco,

        // --- CAMPOS DO ANEXO V (Sem o campo de Cidade/Data) ---
        String campus,          
        String turmaPeriodo,    
        String atividadeEvento, 
        String localidadeEvento, 
        String nomeFamiliar,    
        String contatoFamiliar, 

        // Dados Bancários
        String banco,
        String agencia,
        String conta,

        // Auxílios Solicitados (Checkboxes)
        boolean solicitaInscricao,
        boolean solicitaPassagem,
        boolean solicitaHospedagem,
        boolean solicitaLocomocao,
        boolean solicitaAlimentacao,

        // Período de Afastamento
        String dataSaida,
        String horaSaida,
        String dataChegada,
        String horaChegada
        
) {
    public SolicitacaoIndividualDTO(SolicitacaoIndividual entidade) {
        this(
                entidade.getId(),
                entidade.getViagem() != null ? entidade.getViagem().getId() : null,
                entidade.getData(),
                entidade.getCaminhoArquivo(),
                entidade.getCaminhoArquivoTermo(),
                entidade.getTamanho(),
                entidade.getHash(),
                entidade.getJustificativa(),
                entidade.getSolicitadoEm(),
                entidade.getAfastamento(),
                entidade.getNome(),
                entidade.getCpf(),
                entidade.getMatricula(),
                entidade.getCurso(),
                entidade.getEmail(),
                entidade.getTelefone(),
                entidade.getEndereco(),
                // Novos campos da Entity (Lembre-se de remover o get da Cidade/Data na Entity também)
                entidade.getCampus(),
                entidade.getTurmaPeriodo(),
                entidade.getAtividadeEvento(),
                entidade.getLocalidadeEvento(),
                entidade.getNomeFamiliar(),
                entidade.getContatoFamiliar(),
                
                entidade.getBanco(),
                entidade.getAgencia(),
                entidade.getConta(),
                entidade.isSolicitaInscricao(),
                entidade.isSolicitaPassagem(),
                entidade.isSolicitaHospedagem(),
                entidade.isSolicitaLocomocao(),
                entidade.isSolicitaAlimentacao(),
                entidade.getDataSaida(),
                entidade.getHoraSaida(),
                entidade.getDataChegada(),
                entidade.getHoraChegada()
                
        );
    }
}