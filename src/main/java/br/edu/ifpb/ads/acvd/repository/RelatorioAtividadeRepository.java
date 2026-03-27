package br.edu.ifpb.ads.acvd.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.ads.acvd.entity.RelatorioAtividade;

import java.util.Optional;


public interface RelatorioAtividadeRepository extends JpaRepository<RelatorioAtividade, UUID> {

    Optional<RelatorioAtividade> findByViagemId(UUID viagem);
}
