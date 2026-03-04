package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record RequisicaoDetalhesDTO(
        UUID requisicaoId,
        StatusRequisicao status,
        String motivoReprovacao,
        BigDecimal valorDiaria,
        BigDecimal inscricaoValor,

        DiscenteInfo discente,
        ContaInfo contaBancaria,
        DocumentoInfo documentoDiscente,
        ResponsavelInfo responsavelLegal
) {
    public RequisicaoDetalhesDTO(
            Requisicao requisicao,
            User discente,
            ContaBancaria conta,
            Documento docDiscente,
            ResponsavelLegal responsavel,
            Documento docResponsavel) {
        this(
                requisicao.getId(),
                requisicao.getStatus(),
                requisicao.getMotivoReprovacao(),
                requisicao.getValorDiaria(),
                requisicao.getInscricaoValor(),

                new DiscenteInfo(discente),
                conta != null ? new ContaInfo(conta) : null,
                docDiscente != null ? new DocumentoInfo(docDiscente) : null,
                responsavel != null ? new ResponsavelInfo(responsavel, docResponsavel) : null
        );
    }

    // --- Sub-records para organizar as informações de forma limpa ---

    public record DiscenteInfo(UUID id, String nome, String matricula, String cpf, String rg, Date dataNascimento, String curso, String email, String telefone) {
        public DiscenteInfo(User u) {
            this(u.getUserId(), u.getNome(), u.getMatricula(), u.getNumeroCpf(), u.getNumeroRg(), u.getDataNascimento(), u.getCurso(), u.getEmail(), u.getTelefone());
        }
    }

    public record ContaInfo(String banco, String agencia, String numero, String operacao) {
        public ContaInfo(ContaBancaria c) {
            this(c.getBanco(), c.getAgencia(), c.getNumero(), c.getOperacao());
        }
    }

    public record DocumentoInfo(UUID id, String nomeOriginal, String tamanho, LocalDateTime dataUpload) {
        public DocumentoInfo(Documento d) {
            this(d.getId(), d.getNomeOriginal(), d.getTamanho(), d.getDataUpload());
        }
    }

    public record ResponsavelInfo(String nome, String cpf, String rg, String contato, DocumentoInfo documento) {
        public ResponsavelInfo(ResponsavelLegal r, Documento doc) {
            this(r.getNome(), r.getCpf(), r.getRg(), r.getContato(), doc != null ? new DocumentoInfo(doc) : null);
        }
    }
}