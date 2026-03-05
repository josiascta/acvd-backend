package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitacaoIndividualRepository extends JpaRepository<SolicitacaoIndividual, UUID> {
    Optional<SolicitacaoIndividual> findByViagemId(UUID viagemId);
}