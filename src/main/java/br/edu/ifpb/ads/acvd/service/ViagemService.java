package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.ViagemDTO;
import br.edu.ifpb.ads.acvd.entity.Itinerario;
import br.edu.ifpb.ads.acvd.entity.Role;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
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
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ViagemDTO criarViagem(UUID servidorId, ViagemDTO dto) throws RegraDeNegocioException {
        // 1. Validar se o utilizador existe e é um Servidor
        User responsavel = userRepository.findById(servidorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));

        if (responsavel.getRole() != Role.SERVIDOR) {
            throw new RegraDeNegocioException("Apenas utilizadores com perfil de SERVIDOR podem registar novas viagens.");
        }

        // 2. Validações de datas
        if (dto.dataRetorno().isBefore(dto.dataPartida())) {
            throw new RegraDeNegocioException("A data de retorno não pode ser anterior à data de partida.");
        }

        // 3. Mapear DTO para a Entidade
        Viagem viagem = new Viagem();
        viagem.setDataPartida(dto.dataPartida());
        viagem.setDataRetorno(dto.dataRetorno());
        viagem.setPrazoAnexosDiscentes(dto.prazoAnexosDiscentes());
        viagem.setValorDiariaCnpq(dto.valorDiariaCnpq());
        viagem.setTipoViagem(dto.tipoViagem());
        viagem.setResponsavel(responsavel);

        // 4. Adicionar itinerários utilizando o método auxiliar que gere a relação bidirecional
        if (dto.itinerarios() != null) {
            dto.itinerarios().forEach(itinerarioDTO -> {
                Itinerario itinerario = new Itinerario();
                itinerario.setHorarioEntrada(itinerarioDTO.horarioEntrada());
                itinerario.setHorarioSaida(itinerarioDTO.horarioSaida());
                itinerario.setLocal(itinerarioDTO.local());
                viagem.addItinerario(itinerario);
            });
        }

        // 5. Guardar e retornar o DTO
        Viagem viagemSalva = viagemRepository.save(viagem);
        return new ViagemDTO(viagemSalva);
    }

    public List<ViagemDTO> listarTodas() {
        return viagemRepository.findAll().stream()
                .map(ViagemDTO::new)
                .collect(Collectors.toList());
    }

    public ViagemDTO buscarPorId(UUID viagemId) {
        Viagem viagem = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));
        return new ViagemDTO(viagem);
    }

    // Método útil para o servidor ver as viagens pelas quais é responsável
    public List<ViagemDTO> listarViagensDoServidor(UUID servidorId) {
        // Assume que existe um método customizado no ViagemRepository: findByResponsavelUserId(UUID id)
        // Se ainda não o criou no repositório, bastará adicionar a assinatura correspondente.
        return viagemRepository.findAll().stream()
                .filter(v -> v.getResponsavel().getUserId().equals(servidorId))
                .map(ViagemDTO::new)
                .collect(Collectors.toList());
    }
}