package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.SolicitacaoColetiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitacaoColetivaRepository extends JpaRepository<SolicitacaoColetiva, UUID> {
    Optional<SolicitacaoColetiva> findByViagemId(UUID viagemId);
}