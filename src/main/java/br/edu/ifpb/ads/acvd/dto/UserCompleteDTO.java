package br.edu.ifpb.ads.acvd.dto;

import br.edu.ifpb.ads.acvd.entity.*;

import java.util.Date;
import java.util.UUID;

public record UserCompleteDTO(UUID id,
                              String nome,
                              String fotoDePerfil,
                              String email,
                              String matricula,
                              String telefone,
                              String numeroCpf,
                              String numeroRg,
                              String curso,
                              Date dataNascimento,
                              Role role,
                              ContaBancariaDTO contaBancaria,
                              ResponsavelLegalDTO responsavelLegal,
                              DocumentoDTO documento) {
    public UserCompleteDTO(User user) {
        this(user.getUserId(),
                user.getNome(),
                user.getFotoDePerfil(),
                user.getEmail(),
                user.getMatricula(),
                user.getTelefone(),
                user.getNumeroCpf(),
                user.getNumeroRg(),
                user.getCurso(),
                user.getDataNascimento(),
                user.getRole(),
                user.getContaBancaria() != null ? new ContaBancariaDTO(user.getContaBancaria()) : null,
                user.getResponsavelLegal() != null ? new ResponsavelLegalDTO(user.getResponsavelLegal()) : null,
                user.getDocumento() != null ? new DocumentoDTO(user.getDocumento()) : null);
    }
}
