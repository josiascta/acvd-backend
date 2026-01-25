package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Role;
import br.edu.ifpb.ads.acvd.entity.User;

import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String nome,
                              String fotoDePerfil,
                              String email,
                              String matricula,
                              Role role) {
    public UserResponseDTO(User user) {
        this(user.getUserId(),
                user.getNome(),
                user.getFotoDePerfil(),
                user.getEmail(),
                user.getMatricula(),
                user.getRole());
    }
}
