package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoColetivaDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoColetiva;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoColetivaRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
public class SolicitacaoColetivaService {

    private final SolicitacaoColetivaRepository repository;
    private final ViagemRepository viagemRepository;
    private final PdfSolicitacaoColetivaService pdfService;

    public SolicitacaoColetivaService(SolicitacaoColetivaRepository repository,

                                  ViagemRepository viagemRepository,
                                  PdfSolicitacaoColetivaService pdfService
                                  ) {
    this.repository = repository;
    this.viagemRepository = viagemRepository;
    this.pdfService = pdfService;
}

    @Transactional
    public SolicitacaoColetivaDTO processarSolicitacao(UUID userId, SolicitacaoColetivaDTO dto) {
        SolicitacaoColetiva entidadeSalva = salvarDadosNoBanco(dto);
        return new SolicitacaoColetivaDTO(entidadeSalva);
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdfSobDemanda(UUID solicitacaoId, UUID userId) {
        SolicitacaoColetiva solicitacao = repository.findById(solicitacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação Coletiva não encontrada"));

        // Vai buscar os dados do utilizador responsável através da viagem
        User responsavel = solicitacao.getViagem().getResponsavel();

        // TRAVA DE SEGURANÇA: Verifica se o usuário logado é o verdadeiro responsável pela viagem
        if (!responsavel.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para baixar este documento.");
        }

        try {
            // Passa a solicitação e o utilizador atualizado para o gerador de PDF
            return pdfService.preencherPdf(solicitacao, responsavel);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar PDF", e);
        }
    }

    private SolicitacaoColetiva salvarDadosNoBanco(SolicitacaoColetivaDTO dto) {
        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        SolicitacaoColetiva solicitacao = repository.findByViagemId(viagem.getId())
                .orElse(new SolicitacaoColetiva());

        solicitacao.setViagem(viagem);

        // Removidos os campos redundantes do utilizador (nome, matrícula, etc.)

        solicitacao.setSolicitadoEm(dto.solicitadoEm());
        solicitacao.setAfastamento(dto.afastamento());
        solicitacao.setDisciplinaOuProjeto(dto.disciplinaOuProjeto());
        solicitacao.setSetorDepartamentoCurso(dto.setorDepartamentoCurso());
        solicitacao.setJustificativa(dto.justificativa());

        solicitacao.setInscricao(dto.inscricao());
        solicitacao.setHospedagem(dto.hospedagem());
        solicitacao.setLocomocaoUrbana(dto.locomocaoUrbana());
        solicitacao.setAlimentacao(dto.alimentacao());
        solicitacao.setPassagem(dto.passagem());
        solicitacao.setPlanejamentoVisitaTecnica(dto.planejamentoVisitaTecnica());
        solicitacao.setPlanilha(dto.planilha());
        solicitacao.setTermoResponsabilidade(dto.termoResponsabilidade());
        solicitacao.setOutrosDocumentos(dto.outrosDocumentos());

        return repository.save(solicitacao);
    }

    @Transactional(readOnly = true)
    public SolicitacaoColetivaDTO buscarPorViagemId(UUID viagemId) {

        SolicitacaoColetiva solicitacao = repository.findByViagemId(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação ainda não preenchida."));

        return new SolicitacaoColetivaDTO(solicitacao);
    }
}