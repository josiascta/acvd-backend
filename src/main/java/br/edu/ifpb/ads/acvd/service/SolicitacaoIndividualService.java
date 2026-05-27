package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.RelatorioDiscenteRepository;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class SolicitacaoIndividualService {

    private final RelatorioDiscenteRepository relatorioRepository;
    private final SolicitacaoIndividualRepository repository;
    private final ViagemRepository viagemRepository;
    private final PdfSolicitacaoIndividualService pdfService;
    private final PdfTermoResponsabilidadeService pdfTermoResponsabilidadeService;

    // Construtor limpo (sem lógica de criar pasta no disco)
    public SolicitacaoIndividualService(SolicitacaoIndividualRepository repository,
                                        ViagemRepository viagemRepository,
                                        PdfSolicitacaoIndividualService pdfService,
                                        RelatorioDiscenteRepository relatorioRepository,
                                        PdfTermoResponsabilidadeService pdfTermoResponsabilidadeService) {
        this.repository = repository;
        this.viagemRepository = viagemRepository;
        this.pdfTermoResponsabilidadeService = pdfTermoResponsabilidadeService;
        this.pdfService = pdfService;
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional
    public SolicitacaoIndividualDTO gerarESalvarSolicitacao(SolicitacaoIndividualDTO dto) {
        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));

        SolicitacaoIndividual solicitacao = repository.findByViagemId(viagem.getId())
                .orElse(new SolicitacaoIndividual());

        // Mapeamento dos dados (Continua salvando tudo no banco normalmente)
        solicitacao.setViagem(viagem);
        solicitacao.setNome(dto.nome());
        solicitacao.setCpf(dto.cpf());
        solicitacao.setMatricula(dto.matricula());
        solicitacao.setCurso(dto.curso());
        solicitacao.setEmail(dto.email());
        solicitacao.setTelefone(dto.telefone());
        solicitacao.setEndereco(dto.endereco());
        solicitacao.setBanco(dto.banco());
        solicitacao.setAgencia(dto.agencia());
        solicitacao.setConta(dto.conta());
        solicitacao.setJustificativa(dto.justificativa());
        solicitacao.setSolicitadoEm(dto.solicitadoEm());
        solicitacao.setData(new Date());
        solicitacao.setAfastamento(dto.afastamento());
        
        solicitacao.setSolicitaInscricao(dto.solicitaInscricao());
        solicitacao.setSolicitaPassagem(dto.solicitaPassagem());
        solicitacao.setSolicitaHospedagem(dto.solicitaHospedagem());
        solicitacao.setSolicitaLocomocao(dto.solicitaLocomocao());
        solicitacao.setSolicitaAlimentacao(dto.solicitaAlimentacao());
        solicitacao.setDataSaida(dto.dataSaida());
        solicitacao.setHoraSaida(dto.horaSaida());
        solicitacao.setDataChegada(dto.dataChegada());
        solicitacao.setHoraChegada(dto.horaChegada());

        // ====================================================================
        // SOLUÇÃO DO ERRO 500: Preenche os campos obrigatórios herdados de Anexo
        // ====================================================================
        solicitacao.setCaminhoArquivo("EM_MEMORIA"); 
        solicitacao.setHash("GERADO_SOB_DEMANDA");
        solicitacao.setTamanho("0");
        // ====================================================================

        SolicitacaoIndividual entidadeSalva = repository.save(solicitacao);

        return new SolicitacaoIndividualDTO(repository.save(solicitacao));
    }

    // O CONTROLLER CONTINUA CHAMANDO ESSE MÉTODO, MAS AGORA ELE GERA NA HORA
    public Resource carregarArquivo(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        
        try {
            SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(solicitacao);
            byte[] pdfBytes = pdfService.preencherAnexoII(dto);
            
            // O truque está aqui: passamos os bytes e um nome virtual pro arquivo
            return new CustomByteArrayResource(pdfBytes, "Solicitacao_" + id + ".pdf");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar PDF da Solicitação", e);
        }
    }

    // O CONTROLLER CONTINUA CHAMANDO ESSE MÉTODO TAMBÉM
    public Resource carregarArquivoTermo(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        
        try {
            SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(solicitacao);
            byte[] pdfBytes = pdfTermoResponsabilidadeService.gerarPdfTermo(dto);
            
            return new CustomByteArrayResource(pdfBytes, "Termo_" + id + ".pdf");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar PDF do Termo", e);
        }
    }

    @Transactional
    public void excluir(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        Viagem viagem = solicitacao.getViagem();
        relatorioRepository.findBySolicitacaoId(id).ifPresent(relatorioRepository::delete);

        // REMOVIDO: Tentativa de apagar arquivos locais

        solicitacao.setViagem(null);
        repository.saveAndFlush(solicitacao);
        repository.delete(solicitacao);
        if (viagem != null) viagemRepository.delete(viagem);
    }

    public List<SolicitacaoIndividualDTO> listarPorDiscente() {
        return repository.findAll().stream()
                .filter(s -> s.getViagem() != null)
                .map(SolicitacaoIndividualDTO::new)
                .toList();
    }

    // CLASSE AUXILIAR INTERNA: Permite colocar um nome no recurso em memória
    // para o controller conseguir ler usando o antigo resource.getFilename()
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