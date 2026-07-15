package br.edu.ifpb.ads.acvd.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpb.ads.acvd.entity.RelatorioViagemDiscente;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelatorioDiscenteRepository extends JpaRepository<RelatorioViagemDiscente, Long> {
    // Permite encontrar o relatório de um aluno específico pelo ID da solicitação dele
    Optional<RelatorioViagemDiscente> findBySolicitacaoId(UUID solicitacaoId);
}