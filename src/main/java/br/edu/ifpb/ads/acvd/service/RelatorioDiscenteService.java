package br.edu.ifpb.ads.acvd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.entity.RelatorioViagemDiscente;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.repository.RelatorioDiscenteRepository;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    private PdfSolicitacaoIndividualService pdfService; 

    @Transactional
    public RelatorioViagemDiscente salvar(RelatorioDiscenteDTO dto) {
        // 1. Busca a solicitação vinculada
        SolicitacaoIndividual solicitacao = solicitacaoRepository.findById(dto.solicitacaoId())
            .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        // 2. Busca relatório existente ou cria novo
        RelatorioViagemDiscente relatorio = repository.findBySolicitacaoId(dto.solicitacaoId())
            .orElse(new RelatorioViagemDiscente());

        // 3. Mapeamento dos campos normais
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
        // REMOVIDO: Toda a gravação do arquivo físico na pasta uploads/
        return repository.save(relatorio);
    }

    // NOVO MÉTODO: Gera o PDF do relatório em memória na hora do download
    public Resource carregarArquivoRelatorio(UUID solicitacaoId) {
        RelatorioViagemDiscente relatorio = repository.findBySolicitacaoId(solicitacaoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado"));
        
        try {
            // Monta o DTO com os dados do banco para preencher o PDF
            RelatorioDiscenteDTO dto = new RelatorioDiscenteDTO(
                relatorio.getSolicitacao().getId(),
                relatorio.getDescricaoAtividades(),
                relatorio.getValorAjudaCusto(),
                relatorio.getAjudaCustoExtenso(),
                relatorio.getValorPassagens(),
                relatorio.getPassagensExtenso(),
                relatorio.getNumeroBilhetes(),
                relatorio.getObservacoes()
            );
            
            // Gera os bytes do PDF sob demanda
            byte[] pdfBytes = pdfService.preencherAnexoVII(dto);
            
            // Retorna o recurso virtual usando o CustomByteArrayResource que você já tem no projeto
            return new CustomByteArrayResource(pdfBytes, "Relatorio_Discente_" + solicitacaoId + ".pdf");
            
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar PDF do Relatório", e);
        }
    }

    public Optional<RelatorioViagemDiscente> buscarPorSolicitacaoId(UUID solicitacaoId) {
        return repository.findBySolicitacaoId(solicitacaoId);
    }

    // CLASSE AUXILIAR INTERNA: Mesma estrutura usada para dar nome ao arquivo em memória
    private static class CustomByteArrayResource extends ByteArrayResource {
        private final String filename;

        public CustomByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }
}