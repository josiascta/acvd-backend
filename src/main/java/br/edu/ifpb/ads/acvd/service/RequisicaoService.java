package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.config.BeneficiosConfig;
import br.edu.ifpb.ads.acvd.dto.DiscenteParticipanteDTO;
import br.edu.ifpb.ads.acvd.dto.RequisicaoDTO;
import br.edu.ifpb.ads.acvd.dto.RequisicaoDetalhesDTO;
import br.edu.ifpb.ads.acvd.dto.TermoResponsabilidadeDTO;
import br.edu.ifpb.ads.acvd.entity.*;
import br.edu.ifpb.ads.acvd.exception.RegraDeNegocioException;
import br.edu.ifpb.ads.acvd.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final ViagemRepository viagemRepository;
    private final UserRepository userRepository;

    private final ContaBancariaRepository contaBancariaRepository;
    private final DocumentoRepository documentoRepository;
    private final ResponsavelLegalRepository responsavelLegalRepository;
    private final TermoResponsabilidadeService termoResponsabilidadeService;

    private final BeneficiosConfig beneficiosConfig;

    @Transactional
    public RequisicaoDTO.Response adicionarDiscenteAViagem(UUID servidorId, UUID viagemId, RequisicaoDTO.AdicionarDiscente dto) throws RegraDeNegocioException {
        Viagem viagem = viagemRepository.findById(viagemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));
        if (!viagem.getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Não tem permissão para adicionar alunos a esta viagem.");
        }
        User discente = userRepository.findByMatricula(dto.matriculaDiscente()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discente não encontrado."));
        if (discente.getRole() != Role.DISCENTE) throw new RegraDeNegocioException("O utilizador informado não é um Discente.");
        if (requisicaoRepository.existsByDiscenteUserIdAndViagemId(discente.getUserId(), viagemId)) throw new RegraDeNegocioException("Este discente já foi adicionado a esta viagem.");

        Requisicao requisicao = new Requisicao();
        requisicao.setDiscente(discente);
        requisicao.setViagem(viagem);
        requisicao.setStatus(StatusRequisicao.AGUARDANDO_ENVIO);

        requisicao.setTipoAfastamento(dto.tipoAfastamento());
        requisicao.setInscricaoValor(dto.inscricaoValor());

        this.calcularValoresFinanceiros(requisicao);

        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    @Transactional
    public RequisicaoDTO.Response adicionarDiscenteAViagemPorEmail(UUID servidorId, UUID viagemId, RequisicaoDTO.AdicionarDiscentePorEmail dto) throws RegraDeNegocioException {
        Viagem viagem = viagemRepository.findById(viagemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));
        if (!viagem.getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Não tem permissão para adicionar alunos a esta viagem.");
        }
        User discente = userRepository.findByEmail(dto.emailDiscente()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discente não encontrado."));
        if (discente.getRole() != Role.DISCENTE) throw new RegraDeNegocioException("O utilizador informado não é um Discente.");
        if (requisicaoRepository.existsByDiscenteUserIdAndViagemId(discente.getUserId(), viagemId)) throw new RegraDeNegocioException("Este discente já foi adicionado a esta viagem.");

        Requisicao requisicao = new Requisicao();
        requisicao.setDiscente(discente);
        requisicao.setViagem(viagem);
        requisicao.setStatus(StatusRequisicao.AGUARDANDO_ENVIO);

        requisicao.setTipoAfastamento(dto.tipoAfastamento());
        requisicao.setInscricaoValor(dto.inscricaoValor());

        this.calcularValoresFinanceiros(requisicao);

        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    @Transactional
    public RequisicaoDTO.Response enviarParaAnalise(UUID discenteId, UUID requisicaoId) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        if (!requisicao.getDiscente().getUserId().equals(discenteId)) {
            throw new RegraDeNegocioException("Você não tem permissão para alterar esta requisição.");
        }

        if (requisicao.getStatus() != StatusRequisicao.AGUARDANDO_ENVIO && requisicao.getStatus() != StatusRequisicao.REPROVADO) {
            throw new RegraDeNegocioException("A requisição só pode ser enviada se estiver 'Aguardando Envio' ou 'Reprovada'.");
        }

        ContaBancaria conta = contaBancariaRepository.findByUserUserId(discenteId)
                .orElseThrow(() -> new RegraDeNegocioException("Você precisa cadastrar uma Conta Bancária no seu perfil antes de enviar a requisição."));

        boolean possuiDocumento = documentoRepository.findByUserUserId(discenteId).isPresent();
        if (!possuiDocumento) {
            throw new RegraDeNegocioException("Você precisa fazer o upload do seu documento de identidade no perfil antes de enviar.");
        }

        requisicao.setContaBancaria(conta);
        requisicao.setStatus(StatusRequisicao.AGUARDANDO_ANALISE);
        requisicao.setMotivoReprovacao(null);

        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    @Transactional(readOnly = true)
    public RequisicaoDetalhesDTO obterDetalhesParaAvaliacao(UUID servidorId, UUID requisicaoId) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        if (!requisicao.getViagem().getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Você não tem permissão para ver os detalhes desta requisição.");
        }

        User discente = requisicao.getDiscente();

        ContaBancaria conta = requisicao.getContaBancaria() != null ? requisicao.getContaBancaria() : contaBancariaRepository.findByUserUserId(discente.getUserId()).orElse(null);
        Documento docDiscente = documentoRepository.findByUserUserId(discente.getUserId()).orElse(null);
        ResponsavelLegal responsavel = responsavelLegalRepository.findByUserUserId(discente.getUserId()).orElse(null);
        Documento docResponsavel = responsavel != null ? documentoRepository.findByResponsavelLegalId(responsavel.getId()).orElse(null) : null;

        return new RequisicaoDetalhesDTO(requisicao, discente, conta, docDiscente, responsavel, docResponsavel);
    }

    @Transactional
    public RequisicaoDTO.Response avaliarRequisicao(UUID servidorId, UUID requisicaoId, RequisicaoDTO.Avaliar dto) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));
        if (!requisicao.getViagem().getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Não tem permissão para avaliar as requisições desta viagem.");
        }
        if (dto.status() == StatusRequisicao.REPROVADO && (dto.motivoReprovacao() == null || dto.motivoReprovacao().trim().isEmpty())) {
            throw new RegraDeNegocioException("Para reprovar uma requisição, é obrigatório informar o motivo da reprovação.");
        }
        requisicao.setStatus(dto.status());
        if (dto.status() == StatusRequisicao.REPROVADO) {
            requisicao.setMotivoReprovacao(dto.motivoReprovacao());
        } else {
            requisicao.setMotivoReprovacao(null);
        }
        return new RequisicaoDTO.Response(requisicaoRepository.save(requisicao));
    }

    public List<RequisicaoDTO.Response> listarRequisicoesDaViagem(UUID servidorId, UUID viagemId) {
        return requisicaoRepository.findByViagemId(viagemId).stream().map(RequisicaoDTO.Response::new).collect(Collectors.toList());
    }

    public List<RequisicaoDTO.Response> listarMinhasRequisicoes(UUID discenteId) {
        return requisicaoRepository.findByDiscenteUserId(discenteId).stream().map(RequisicaoDTO.Response::new).collect(Collectors.toList());
    }

    public List<DiscenteParticipanteDTO> listarDiscentesParticipantes(UUID viagemId) {
        return requisicaoRepository.findByViagemId(viagemId).stream()
                .map(req -> {
                    User discente = req.getDiscente();
                    ContaBancaria conta = req.getContaBancaria() != null ? req.getContaBancaria() : discente.getContaBancaria();

                    return new DiscenteParticipanteDTO(discente, conta);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TermoResponsabilidadeDTO uploadTermoResponsabilidade(UUID discenteId, UUID requisicaoId, MultipartFile file) throws Exception {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        if (!requisicao.getDiscente().getUserId().equals(discenteId)) {
            throw new RegraDeNegocioException("Você não tem permissão para alterar esta requisição.");
        }

        if (requisicao.getTermoResponsabilidade() != null) {
            termoResponsabilidadeService.removerArquivoFisico(requisicao.getTermoResponsabilidade());
            requisicao.setTermoResponsabilidade(null);
            requisicaoRepository.saveAndFlush(requisicao);
        }

        String prefixo = "termo_req_" + requisicaoId;
        TermoResponsabilidade termo = termoResponsabilidadeService.processarUpload(file, prefixo);

        requisicao.setTermoResponsabilidade(termo);
        requisicaoRepository.save(requisicao);

        return new TermoResponsabilidadeDTO(termo);
    }

    @Transactional(readOnly = true)
    public Resource baixarTermoResponsabilidade(UUID userId, UUID requisicaoId) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        boolean isDono = requisicao.getDiscente().getUserId().equals(userId);

        boolean isResponsavelViagem = requisicao.getViagem().getResponsavel().getUserId().equals(userId);

        if (!isDono && !isResponsavelViagem) {
            throw new RegraDeNegocioException("Não tem permissão para descarregar o termo de responsabilidade desta requisição.");
        }

        TermoResponsabilidade termo = requisicao.getTermoResponsabilidade();
        if (termo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esta requisição ainda não possui um Termo de Responsabilidade anexado.");
        }

        return termoResponsabilidadeService.carregarArquivo(termo);
    }

    @Transactional
    public void removerDiscenteDaViagem(UUID servidorId, UUID requisicaoId) throws RegraDeNegocioException {
        Requisicao requisicao = requisicaoRepository.findById(requisicaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        if (!requisicao.getViagem().getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Não tem permissão para remover alunos desta viagem.");
        }

        if (requisicao.getStatus() == StatusRequisicao.APROVADA) {
            throw new RegraDeNegocioException("Não é possível remover um discente cuja requisição já se encontre aprovada.");
        }

        if (requisicao.getTermoResponsabilidade() != null) {
            termoResponsabilidadeService.removerArquivoFisico(requisicao.getTermoResponsabilidade());
        }

        requisicaoRepository.delete(requisicao);
    }

    @Transactional(readOnly = true)
    public byte[] baixarDocumentosViagemZip(UUID servidorId, UUID viagemId) throws RegraDeNegocioException, IOException {
        Viagem viagem = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));

        if (!viagem.getResponsavel().getUserId().equals(servidorId)) {
            throw new RegraDeNegocioException("Você não tem permissão para baixar os documentos desta viagem.");
        }

        List<Requisicao> requisicoes = requisicaoRepository.findByViagemId(viagemId);

        if (requisicoes.isEmpty()) {
            throw new RegraDeNegocioException("Não há discentes vinculados a esta viagem.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            requisicoes.forEach(req -> {
                User discente = req.getDiscente();
                String nomeLimpo = discente.getNome().replaceAll("[^a-zA-Z0-9 ]", "").trim();
                String pastaAluno = nomeLimpo + " - " + discente.getMatricula() + "/";

                try {
                    zos.putNextEntry(new java.util.zip.ZipEntry(pastaAluno));
                    zos.closeEntry();

                    documentoRepository.findByUserUserId(discente.getUserId()).ifPresent(docPessoal -> {
                        adicionarArquivoAoZip(zos, docPessoal.getCaminhoDoArquivo(),
                                pastaAluno + "Identidade_Discente_" + docPessoal.getNomeOriginal());
                    });

                    TermoResponsabilidade termo = req.getTermoResponsabilidade();
                    if (termo != null) {
                        adicionarArquivoAoZip(zos, termo.getCaminhoArquivo(),
                                pastaAluno + "Anexo_V_Termo_Responsabilidade.pdf");
                    }

                    responsavelLegalRepository.findByUserUserId(discente.getUserId()).ifPresent(responsavel -> {
                        documentoRepository.findByResponsavelLegalId(responsavel.getId()).ifPresent(docResp -> {
                            adicionarArquivoAoZip(zos, docResp.getCaminhoDoArquivo(),
                                    pastaAluno + "Identidade_Responsavel_" + docResp.getNomeOriginal());
                        });
                    });

                } catch (IOException e) {
                    System.err.println("Erro ao processar documentos do discente: " + discente.getNome());
                }
            });
        }

        return baos.toByteArray();
    }

    private void adicionarArquivoAoZip(java.util.zip.ZipOutputStream zos, String caminhoFisico, String nomeNoZip) {
        if (caminhoFisico == null || caminhoFisico.isBlank()) return;

        try {
            java.nio.file.Path path = java.nio.file.Paths.get(caminhoFisico);
            if (java.nio.file.Files.exists(path)) {
                java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(nomeNoZip);
                zos.putNextEntry(entry);
                java.nio.file.Files.copy(path, zos);
                zos.closeEntry();
            }
        } catch (IOException e) {
            System.err.println("Erro ao adicionar arquivo ao ZIP (" + caminhoFisico + "): " + e.getMessage());
        }
    }

    private void calcularValoresFinanceiros(Requisicao requisicao) {
        // Converte os valores da configuração (Double) para BigDecimal
        BigDecimal valorBase = BigDecimal.valueOf(beneficiosConfig.getValorDiariaCnpq());
        BigDecimal tetoInscricao = BigDecimal.valueOf(beneficiosConfig.getTetoInscricao());

        // 1. Cálculo da Diária com base no Tipo de Afastamento
        if (requisicao.getTipoAfastamento() != null) {
            String nomeEnum = requisicao.getTipoAfastamento().name();
            // Busca o percentual no Map da configuração. Se não achar, usa 0.0
            Double percentualDouble = beneficiosConfig.getPercentuais().getOrDefault(nomeEnum, 0.0);
            BigDecimal percentual = BigDecimal.valueOf(percentualDouble);

            // Aplica a regra: valorBase * percentual
            requisicao.setValorDiaria(valorBase.multiply(percentual));
        } else {
            requisicao.setValorDiaria(BigDecimal.ZERO);
        }

        // 2. Lógica da Inscrição (Apoio financeiro para eventos)
        if (requisicao.getInscricaoValor() != null && requisicao.getInscricaoValor().compareTo(BigDecimal.ZERO) > 0) {
            requisicao.setSolicitaInscricao(true);

            // Regra do Art. 9: Se o valor solicitado for maior que o teto (R$ 285), trava no teto
            if (requisicao.getInscricaoValor().compareTo(tetoInscricao) > 0) {
                requisicao.setInscricaoValor(tetoInscricao);
            }
        } else {
            // Se veio nulo ou menor/igual a zero, não solicita inscrição
            requisicao.setSolicitaInscricao(false);
            requisicao.setInscricaoValor(BigDecimal.ZERO);
        }
    }
}