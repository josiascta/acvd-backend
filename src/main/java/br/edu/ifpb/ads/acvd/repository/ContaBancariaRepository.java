package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, UUID> {
    Optional<ContaBancaria> findByUserUserId(UUID userId);
}