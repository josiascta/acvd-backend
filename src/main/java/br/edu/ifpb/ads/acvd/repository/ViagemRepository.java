package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ViagemRepository extends JpaRepository<Viagem, UUID> {
    List<Viagem> findByResponsavelUserId(UUID servidorId);
}