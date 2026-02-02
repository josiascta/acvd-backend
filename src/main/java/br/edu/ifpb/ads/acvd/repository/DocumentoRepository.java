package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {
    List<Documento> findByUserUserId(UUID userId);
}
