package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.Requisicao;
import br.edu.ifpb.ads.acvd.entity.StatusRequisicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, UUID> {
    boolean existsByDiscenteUserIdAndViagemId(UUID discenteId, UUID viagemId);
    boolean existsByDiscenteUserIdAndStatus(UUID discenteId, StatusRequisicao status);
    List<Requisicao> findByDiscenteUserId(UUID discenteId);
}