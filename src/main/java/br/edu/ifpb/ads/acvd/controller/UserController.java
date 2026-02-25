package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.UpdateProfileDTO;
import br.edu.ifpb.ads.acvd.dto.UserCompleteDTO;
import br.edu.ifpb.ads.acvd.dto.UserResponseDTO;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(userService.getMyProfile(jwt), HttpStatus.OK);
    }

    @GetMapping("/meComplete")
    public ResponseEntity<UserCompleteDTO> getMyProfileComplete(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(userService.getMyProfileComplete(jwt), HttpStatus.OK);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody UpdateProfileDTO dto) {
        var userId = UUID.fromString(jwt.getSubject());

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (dto.matricula() != null && !dto.matricula().isBlank()) {
            user.setMatricula(dto.matricula());
        }
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    // Deletar após testes
    @DeleteMapping("/test/email/{email}")
    public ResponseEntity<Void> deleteUserTemporario(@PathVariable String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.noContent().build();
    }
}



