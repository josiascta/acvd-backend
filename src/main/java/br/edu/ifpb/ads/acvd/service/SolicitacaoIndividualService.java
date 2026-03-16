package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.RelatorioDiscenteRepository;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
 import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class SolicitacaoIndividualService {
private final RelatorioDiscenteRepository relatorioRepository;
    private final SolicitacaoIndividualRepository repository;
    private final ViagemRepository viagemRepository;
    private final PdfSolicitacaoIndividualService pdfService;
    private final Path fileStorageLocation;

    public SolicitacaoIndividualService(SolicitacaoIndividualRepository repository,
                                        ViagemRepository viagemRepository,
                                        PdfSolicitacaoIndividualService pdfService,
                                        RelatorioDiscenteRepository relatorioRepository,
                                        @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.repository = repository;
        this.viagemRepository = viagemRepository;
        this.pdfService = pdfService;
        this.relatorioRepository = relatorioRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao criar pasta de uploads", ex);
        }
    }

    @Transactional
    public SolicitacaoIndividualDTO gerarESalvarSolicitacao(SolicitacaoIndividualDTO dto) {
        Viagem viagem = viagemRepository.findById(dto.viagemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        SolicitacaoIndividual solicitacao = repository.findByViagemId(viagem.getId())
                .orElse(new SolicitacaoIndividual());

        try {
            // 1. Sincronizar dados da Entity (Anexo II + Anexo V)
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
   
            solicitacao.setContatoFamiliar(dto.contatoFamiliar());

// --- ADICIONE ESTAS LINHAS PARA RESOLVER O ERRO 500 ---
            solicitacao.setAfastamento(dto.afastamento()); // OBRIGATÓRIO (nullable = false)
            solicitacao.setSolicitaInscricao(dto.solicitaInscricao());
            solicitacao.setSolicitaPassagem(dto.solicitaPassagem());
            solicitacao.setSolicitaHospedagem(dto.solicitaHospedagem());
            solicitacao.setSolicitaLocomocao(dto.solicitaLocomocao());
            solicitacao.setSolicitaAlimentacao(dto.solicitaAlimentacao());

            solicitacao.setDataSaida(dto.dataSaida());
            solicitacao.setHoraSaida(dto.horaSaida());
            solicitacao.setDataChegada(dto.dataChegada());
            solicitacao.setHoraChegada(dto.horaChegada());


            // Campos específicos do Anexo V
            solicitacao.setCampus(dto.campus());
            solicitacao.setTurmaPeriodo(dto.turmaPeriodo());
            solicitacao.setAtividadeEvento(dto.atividadeEvento());
            solicitacao.setLocalidadeEvento(dto.localidadeEvento());
            solicitacao.setNomeFamiliar(dto.nomeFamiliar());
            solicitacao.setContatoFamiliar(dto.contatoFamiliar());

            // 2. Gerar os bytes dos dois PDFs
            byte[] bytesAnexoII = pdfService.preencherAnexoII(dto);
            byte[] bytesAnexoV = pdfService.preencherAnexoV(dto);

            // 3. Definir caminhos com nomes fixos para permitir edição (sobrescrita)
            Path pathII = this.fileStorageLocation.resolve(viagem.getId() + "_AnexoII.pdf");
            Path pathV = this.fileStorageLocation.resolve(viagem.getId() + "_AnexoV.pdf");

            // 4. Gravar no disco (TRUNCATE_EXISTING limpa o arquivo antigo se existir)
            Files.write(pathII, bytesAnexoII, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(pathV, bytesAnexoV, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 5. Atualizar caminhos e metadados
            solicitacao.setCaminhoArquivo(pathII.toString());
            solicitacao.setCaminhoArquivoTermo(pathV.toString());
            solicitacao.setHash(gerarHash(bytesAnexoII));
            solicitacao.setTamanho(formatarTamanho(bytesAnexoII.length + bytesAnexoV.length));

            return new SolicitacaoIndividualDTO(repository.save(solicitacao));

        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar arquivos", ex);
        }
    }

    // --- NOVOS MÉTODOS ADICIONADOS PARA DOWNLOAD ---

    public Resource carregarArquivo(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        return carregarRecurso(solicitacao.getCaminhoArquivo());
    }

    public Resource carregarArquivoTermo(UUID id) {
        SolicitacaoIndividual solicitacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
        
        if (solicitacao.getCaminhoArquivoTermo() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo do Termo (Anexo V) não disponível.");
        }
        return carregarRecurso(solicitacao.getCaminhoArquivoTermo());
    }

    private Resource carregarRecurso(String caminho) {
        try {
            Path filePath = Paths.get(caminho);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo físico não encontrado.");
            }
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Caminho do arquivo inválido.", ex);
        }
    }

    // --- MÉTODOS DE UTILITÁRIO (HASH E TAMANHO) ---

    private String gerarHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String formatarTamanho(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "kB", "MB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
   
// ... outros imports

public List<SolicitacaoIndividualDTO> listarPorDiscente() {
    // 1. Pega o usuário logado do Spring Security
    Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID discenteId = UUID.fromString(jwt.getSubject());

    // 2. Busca no repository (ajuste o nome do campo conforme sua Entity)
    // Se no repository o método for findByUsuarioId, use ele aqui.
    return repository.findAll() 
            .stream()
            .filter(s -> s.getViagem() != null)
            // Aqui você pode adicionar um filtro para garantir que só venham as dele
            // .filter(s -> s.getUsuarioId().equals(discenteId)) 
            .map(SolicitacaoIndividualDTO::new)
            .toList();
}
@Transactional
public void excluir(UUID id) {
    SolicitacaoIndividual solicitacao = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

    Viagem viagem = solicitacao.getViagem();

    // 1. APAGA O RELATÓRIO DO BANCO (ANEXO VII) - Isso mata o erro do Postgres
    relatorioRepository.findBySolicitacaoId(id).ifPresent(relatorio -> {
        relatorioRepository.delete(relatorio);
    });

    // 2. LIMPA OS ARQUIVOS FÍSICOS (PDFs)
    try {
        if (solicitacao.getCaminhoArquivo() != null) {
            Files.deleteIfExists(Paths.get(solicitacao.getCaminhoArquivo()));
        }
        if (solicitacao.getCaminhoArquivoTermo() != null) {
            Files.deleteIfExists(Paths.get(solicitacao.getCaminhoArquivoTermo()));
        }
        Path pathRelatorio = this.fileStorageLocation.resolve(id.toString() + "-relatorio-discente.pdf");
        Files.deleteIfExists(pathRelatorio);
    } catch (IOException e) {
        System.err.println("Aviso: Falha ao deletar arquivos: " + e.getMessage());
    }

    // 3. DESVINCULA A VIAGEM DA SOLICITAÇÃO
    solicitacao.setViagem(null);
    repository.saveAndFlush(solicitacao);

    // 4. APAGA A SOLICITAÇÃO
    repository.delete(solicitacao);

    // 5. APAGA A VIAGEM (Os itinerários morrem aqui pelo Cascade ALL na Entity Viagem)
    if (viagem != null) {
        viagemRepository.delete(viagem);
    }
}
}