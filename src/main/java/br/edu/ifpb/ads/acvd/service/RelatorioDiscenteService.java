package br.edu.ifpb.ads.acvd.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.entity.RelatorioViagemDiscente;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.repository.RelatorioDiscenteRepository;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;


@Service
public class RelatorioDiscenteService {

    @Autowired
    private RelatorioDiscenteRepository repository;

    @Autowired
    private SolicitacaoIndividualRepository solicitacaoRepository;

    @Autowired
    private PdfSolicitacaoIndividualService pdfService; // Injeção do serviço de PDF

    @Transactional
    public RelatorioViagemDiscente salvar(RelatorioDiscenteDTO dto) {
        // 1. Busca a solicitação vinculada
        SolicitacaoIndividual solicitacao = solicitacaoRepository.findById(dto.solicitacaoId())
            .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        // 2. Busca relatório existente ou cria novo
        RelatorioViagemDiscente relatorio = repository.findBySolicitacaoId(dto.solicitacaoId())
            .orElse(new RelatorioViagemDiscente());

        // 3. Mapeamento dos campos
        relatorio.setSolicitacao(solicitacao);
        relatorio.setDescricaoAtividades(dto.descricaoAtividades());
        relatorio.setValorAjudaCusto(dto.valorAjudaCusto());
        relatorio.setAjudaCustoExtenso(dto.ajudaCustoExtenso());
        relatorio.setValorPassagens(dto.valorPassagens());
        relatorio.setPassagensExtenso(dto.passagensExtenso());
        relatorio.setNumeroBilhetes(dto.numeroBilhetes());
        relatorio.setObservacoes(dto.observacoes());
        relatorio.setDataRelatorio(LocalDate.now());

        // 4. Salva no Banco de Dados
        RelatorioViagemDiscente relatorioSalvo = repository.save(relatorio);

        // 5. GERA E SALVA O ARQUIVO FÍSICO NA PASTA UPLOADS
        try {
            // Gera os bytes do PDF preenchido
            byte[] pdfBytes = pdfService.preencherAnexoVII(dto);

            // Define o nome do arquivo (ex: id-relatorio-discente.pdf)
            // Usamos o ID da solicitação para manter o padrão que você já tem
            String nomeArquivo = solicitacao.getId().toString() + "-relatorio-discente.pdf";
            
            // Define o caminho (Cria a pasta uploads se não existir)
            java.nio.file.Path path = java.nio.file.Paths.get("uploads/" + nomeArquivo);
            java.nio.file.Files.createDirectories(path.getParent());
            
            // Escreve o arquivo no disco
            java.nio.file.Files.write(path, pdfBytes);
            
            System.out.println("Arquivo salvo em: " + path.toAbsolutePath());

        } catch (Exception e) {
            // Log de erro, mas não interrompe a transação do banco se o PDF falhar
            System.err.println("Erro ao salvar arquivo físico: " + e.getMessage());
        }

        return relatorioSalvo;
    }

    public Optional<RelatorioViagemDiscente> buscarPorSolicitacaoId(UUID solicitacaoId) {
        return repository.findBySolicitacaoId(solicitacaoId);
    }
}