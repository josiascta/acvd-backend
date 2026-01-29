package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.Role;
import br.edu.ifpb.ads.acvd.entity.User;

import java.util.Date;
import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String nome,
                              String fotoDePerfil,
                              String email,
                              String matricula,
                              Date dataNascimento,
                              Role role) {
    public UserResponseDTO(User user) {
        this(user.getUserId(),
                user.getNome(),
                user.getFotoDePerfil(),
                user.getEmail(),
                user.getMatricula(),
                user.getDataNascimento(),
                user.getRole());
    }
}
