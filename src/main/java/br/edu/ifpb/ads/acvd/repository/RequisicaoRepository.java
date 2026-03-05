package br.edu.ifpb.ads.acvd.repository;

import br.edu.ifpb.ads.acvd.entity.Requisicao;
import br.edu.ifpb.ads.acvd.entity.StatusRequisicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, UUID> {

    // Verifica se um discente já foi adicionado a uma determinada viagem
    boolean existsByDiscenteUserIdAndViagemId(UUID discenteId, UUID viagemId);

    // Para o Discente ver as suas próprias requisições
    List<Requisicao> findByDiscenteUserId(UUID discenteId);

    // Para o Servidor listar todos os alunos associados a uma viagem específica
    List<Requisicao> findByViagemId(UUID viagemId);

    // Verifica se o aluno tem alguma requisição num status específico (para travar a conta bancária)
    boolean existsByDiscenteUserIdAndStatus(UUID discenteId, StatusRequisicao status);
}