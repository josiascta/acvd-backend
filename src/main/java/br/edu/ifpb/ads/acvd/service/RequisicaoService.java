package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.RequisicaoDTO;
import br.edu.ifpb.ads.acvd.dto.RequisicaoDetalhesDTO;
import br.edu.ifpb.ads.acvd.entity.*;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
import br.edu.ifpb.ads.acvd.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final ViagemRepository viagemRepository;
    private final UserRepository userRepository;

    private final ContaBancariaRepository contaBancariaRepository;
    private final DocumentoRepository documentoRepository;
    private final ResponsavelLegalRepository responsavelLegalRepository;

    @Transactional
    public RequisicaoDTO.Response adicionarDiscenteAViagem(UUID servidorId, UUID viagemId, RequisicaoDTO.AdicionarDiscente dto) throws RegraDeNegocioException {
        Viagem viagem = viagemRepository.findById(viagemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));
        if (!viagem.getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Não tem permissão para adicionar alunos a esta viagem.");
        }
        User discente = userRepository.findByMatricula(dto.matriculaDiscente()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discente não encontrado."));
        if (discente.getRole() != Role.DISCENTE) throw new RegraDeNegocioException("O utilizador informado não é um Discente.");
        if (requisicaoRepository.existsByDiscenteUserIdAndViagemId(discente.getUserId(), viagemId)) throw new RegraDeNegocioException("Este discente já foi adicionado a esta viagem.");

        Requisicao requisicao = new Requisicao();
        requisicao.setDiscente(discente);
        requisicao.setViagem(viagem);
        requisicao.setStatus(StatusRequisicao.AGUARDANDO_ENVIO);
        requisicao.setValorDiaria(dto.valorDiaria());
        requisicao.setInscricaoValor(dto.inscricaoValor());

        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    @Transactional
    public RequisicaoDTO.Response adicionarDiscenteAViagemPorEmail(UUID servidorId, UUID viagemId, RequisicaoDTO.AdicionarDiscentePorEmail dto) throws RegraDeNegocioException {
        Viagem viagem = viagemRepository.findById(viagemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));
        if (!viagem.getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Não tem permissão para adicionar alunos a esta viagem.");
        }
        User discente = userRepository.findByEmail(dto.emailDiscente()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discente não encontrado."));
        if (discente.getRole() != Role.DISCENTE) throw new RegraDeNegocioException("O utilizador informado não é um Discente.");
        if (requisicaoRepository.existsByDiscenteUserIdAndViagemId(discente.getUserId(), viagemId)) throw new RegraDeNegocioException("Este discente já foi adicionado a esta viagem.");

        Requisicao requisicao = new Requisicao();
        requisicao.setDiscente(discente);
        requisicao.setViagem(viagem);
        requisicao.setStatus(StatusRequisicao.AGUARDANDO_ENVIO);
        requisicao.setValorDiaria(dto.valorDiaria());
        requisicao.setInscricaoValor(dto.inscricaoValor());

        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    @Transactional
    public RequisicaoDTO.Response enviarParaAnalise(UUID discenteId, UUID requisicaoId) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        if (!requisicao.getDiscente().getUserId().equals(discenteId)) {
            throw new RegraDeNegocioException("Você não tem permissão para alterar esta requisição.");
        }

        if (requisicao.getStatus() != StatusRequisicao.AGUARDANDO_ENVIO && requisicao.getStatus() != StatusRequisicao.REPROVADO) {
            throw new RegraDeNegocioException("A requisição só pode ser enviada se estiver 'Aguardando Envio' ou 'Reprovada'.");
        }

        ContaBancaria conta = contaBancariaRepository.findByUserUserId(discenteId)
                .orElseThrow(() -> new RegraDeNegocioException("Você precisa cadastrar uma Conta Bancária no seu perfil antes de enviar a requisição."));

        boolean possuiDocumento = documentoRepository.findByUserUserId(discenteId).isPresent();
        if (!possuiDocumento) {
            throw new RegraDeNegocioException("Você precisa fazer o upload do seu documento de identidade no perfil antes de enviar.");
        }

        requisicao.setContaBancaria(conta);
        requisicao.setStatus(StatusRequisicao.AGUARDANDO_ANALISE);
        requisicao.setMotivoReprovacao(null);

        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    @Transactional(readOnly = true)
    public RequisicaoDetalhesDTO obterDetalhesParaAvaliacao(UUID servidorId, UUID requisicaoId) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        if (!requisicao.getViagem().getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Você não tem permissão para ver os detalhes desta requisição.");
        }

        User discente = requisicao.getDiscente();

        ContaBancaria conta = requisicao.getContaBancaria() != null ? requisicao.getContaBancaria() : contaBancariaRepository.findByUserUserId(discente.getUserId()).orElse(null);
        Documento docDiscente = documentoRepository.findByUserUserId(discente.getUserId()).orElse(null);
        ResponsavelLegal responsavel = responsavelLegalRepository.findByUserUserId(discente.getUserId()).orElse(null);
        Documento docResponsavel = responsavel != null ? documentoRepository.findByResponsavelLegalId(responsavel.getId()).orElse(null) : null;

        return new RequisicaoDetalhesDTO(requisicao, discente, conta, docDiscente, responsavel, docResponsavel);
    }

    @Transactional
    public RequisicaoDTO.Response avaliarRequisicao(UUID servidorId, UUID requisicaoId, RequisicaoDTO.Avaliar dto) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));
        if (!requisicao.getViagem().getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Não tem permissão para avaliar as requisições desta viagem.");
        }
        if (dto.status() == StatusRequisicao.REPROVADO && (dto.motivoReprovacao() == null || dto.motivoReprovacao().trim().isEmpty())) {
            throw new RegraDeNegocioException("Para reprovar uma requisição, é obrigatório informar o motivo da reprovação.");
        }
        requisicao.setStatus(dto.status());
        if (dto.status() == StatusRequisicao.REPROVADO) {
            requisicao.setMotivoReprovacao(dto.motivoReprovacao());
        } else {
            requisicao.setMotivoReprovacao(null);
        }
        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    public List<RequisicaoDTO.Response> listarRequisicoesDaViagem(UUID servidorId, UUID viagemId) {
        return requisicaoRepository.findByViagemId(viagemId).stream().map(RequisicaoDTO.Response::new).collect(Collectors.toList());
    }

    public List<RequisicaoDTO.Response> listarMinhasRequisicoes(UUID discenteId) {
        return requisicaoRepository.findByDiscenteUserId(discenteId).stream().map(RequisicaoDTO.Response::new).collect(Collectors.toList());
    }
}