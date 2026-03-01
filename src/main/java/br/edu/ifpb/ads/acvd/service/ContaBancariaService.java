package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.ContaBancariaDTO;
import br.edu.ifpb.ads.acvd.entity.ContaBancaria;
import br.edu.ifpb.ads.acvd.entity.StatusRequisicao;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
import br.edu.ifpb.ads.acvd.repository.ContaBancariaRepository;
import br.edu.ifpb.ads.acvd.repository.RequisicaoRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContaBancariaService {

    private final ContaBancariaRepository contaBancariaRepository;
    private final UserRepository userRepository;
    private final RequisicaoRepository requisicaoRepository;

    public ContaBancariaDTO obter(UUID userId) {
        ContaBancaria conta = contaBancariaRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta bancária não cadastrada."));
        return new ContaBancariaDTO(conta);
    }

    @Transactional
    public ContaBancariaDTO atualizarConta(UUID userId, ContaBancariaDTO dto) throws RegraDeNegocioException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        // Regra de Negócio: Impede edição se existir requisição aprovada
        boolean possuiRequisicaoAprovada = requisicaoRepository.existsByDiscenteUserIdAndStatus(userId, StatusRequisicao.APROVADA);
        if (possuiRequisicaoAprovada) {
            throw new RegraDeNegocioException("Você não pode editar a conta bancária enquanto possuir uma requisição aprovada em andamento.");
        }

        ContaBancaria conta = contaBancariaRepository.findByUserUserId(userId)
                .orElse(new ContaBancaria());

        conta.setBanco(dto.banco());
        conta.setAgencia(dto.agencia());
        conta.setNumero(dto.numero());
        conta.setOperacao(dto.operacao());
        conta.setUser(user);

        return new ContaBancariaDTO(contaBancariaRepository.save(conta));
    }
}