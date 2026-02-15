package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {
    Optional<Documento> findByUserUserId(UUID userId);
}
