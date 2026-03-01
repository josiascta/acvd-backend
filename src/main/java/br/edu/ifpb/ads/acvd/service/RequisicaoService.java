package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.RequisicaoCreateDTO;
import br.edu.ifpb.ads.acvd.dto.RequisicaoDTO;
import br.edu.ifpb.ads.acvd.entity.*;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
import br.edu.ifpb.ads.acvd.repository.ContaBancariaRepository;
import br.edu.ifpb.ads.acvd.repository.RequisicaoRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
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

    @Transactional
    public RequisicaoDTO criarRequisicao(UUID discenteId, RequisicaoCreateDTO dto) throws RegraDeNegocioException {
        User discente = userRepository.findById(discenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (discente.getRole() != Role.DISCENTE) {
            throw new RegraDeNegocioException("Apenas usuários do tipo DISCENTE podem criar uma requisição.");
        }

        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        if (requisicaoRepository.existsByDiscenteUserIdAndViagemId(discenteId, dto.viagemId())) {
            throw new RegraDeNegocioException("Permitido apenas uma requisição por discente para a mesma viagem.");
        }

        ContaBancaria contaBancaria = contaBancariaRepository.findByUserUserId(discenteId)
                .orElseThrow(() -> new RegraDeNegocioException("É necessário cadastrar uma conta bancária antes de solicitar a requisição."));

        Requisicao requisicao = new Requisicao();
        requisicao.setDiscente(discente);
        requisicao.setViagem(viagem);
        requisicao.setContaBancaria(contaBancaria);
        requisicao.setAfastamento(dto.afastamento());
        requisicao.setPercentualDiaria(dto.percentualDiaria());
        requisicao.setValorDiaria(dto.valorDiaria());
        requisicao.setInscricaoValor(dto.inscricaoValor());
        requisicao.setSolicitaIncricao(dto.solicitaIncricao());
        requisicao.setStatus(StatusRequisicao.AGUARDANDO_ENVIO);

        return new RequisicaoDTO(requisicaoRepository.save(requisicao));
    }

    public List<RequisicaoDTO> listarMinhasRequisicoes(UUID discenteId) {
        return requisicaoRepository.findByDiscenteUserId(discenteId).stream()
                .map(RequisicaoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public RequisicaoDTO confirmarRequisicao(UUID requisicaoId) {
        Requisicao req = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não localizada."));
        req.setStatus(StatusRequisicao.APROVADA);
        return new RequisicaoDTO(requisicaoRepository.save(req));
    }
}