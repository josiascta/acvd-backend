package br.edu.ifpb.ads.acvd.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.ads.acvd.entity.PlanejamentoAtividade;

public interface PlanejamentoAtividadeRepository extends JpaRepository<PlanejamentoAtividade, UUID>{
    
    Optional<PlanejamentoAtividade> findByViagemId(UUID viagemId);
}
