package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.UserCompleteDTO;
import br.edu.ifpb.ads.acvd.dto.UserResponseDTO;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO getMyProfile(Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return new UserResponseDTO(user);
    }

    public UserCompleteDTO getMyProfileComplete(Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return new UserCompleteDTO(user);
    }

    // Deletar após testes
    @Transactional
    public void deleteUserByEmail(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(
                userRepository::delete,
                () -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado via e-mail fornecido.");
                }
        );
    }
}
