package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.ResponsavelLegal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResponsavelLegalRepository extends JpaRepository<ResponsavelLegal, UUID> {
    Optional<ResponsavelLegal> findByUserUserId(UUID userId);
}