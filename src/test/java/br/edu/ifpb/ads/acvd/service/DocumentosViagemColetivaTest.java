package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.DiscenteParticipanteDTO;
import br.edu.ifpb.ads.acvd.entity.*;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para geração de documentos (PDFs) de Viagem Coletiva (Servidores/Campo)")
public class DocumentosViagemColetivaTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViagemRepository viagemRepository;

    @InjectMocks
    private PdfSolicitacaoColetivaService pdfSolicitacaoColetivaService;

    @InjectMocks
    private PdfPlanejamentoAtividade pdfPlanejamentoAtividade;

    @InjectMocks
    private DiscentesParticipantesService discentesParticipantesService;

    @InjectMocks
    private PdfTermoResponsabilidadeService pdfTermoResponsabilidadeService;

    @InjectMocks
    private PdfRelatorioAtividade pdfRelatorioAtividade;

    @Test
    @DisplayName("Anexo I: Solicitação Coletiva de Ajuda de Custo")
    public void deveGerarAnexoISolicitacaoColetivaAjudaCusto() throws IOException {
        SolicitacaoColetiva solicitacao = new SolicitacaoColetiva();
        solicitacao.setId(UUID.randomUUID());
        solicitacao.setSolicitadoEm(new Date());
        solicitacao.setAfastamento(TipoAfastamento.MAIOR_08_HORAS);
        solicitacao.setInscricao(false);
        solicitacao.setHospedagem(true);
        solicitacao.setLocomocaoUrbana(true);
        solicitacao.setAlimentacao(true);
        solicitacao.setPassagem(false);
        solicitacao.setPlanejamentoVisitaTecnica(true);
        solicitacao.setPlanilha(true);
        solicitacao.setTermoResponsabilidade(true);
        solicitacao.setOutrosDocumentos(false);
        solicitacao.setDisciplinaOuProjeto(TipoAtividade.DISCIPLINA);
        solicitacao.setJustificativa("Visita técnica para consolidação prática dos conteúdos de redes de computadores.");
        solicitacao.setSetorDepartamentoCurso("Coordenação do Curso de Análise e Desenvolvimento de Sistemas");

        User responsavel = new User();
        responsavel.setNome("Professor Responsável Teste");
        responsavel.setCurso("Análise e Desenvolvimento de Sistemas");
        responsavel.setEmail("professor@ifpb.edu.br");
        responsavel.setMatricula("1987654");
        responsavel.setTelefone("83977777777");
        responsavel.setRole(Role.SERVIDOR);

        byte[] pdfBytes = pdfSolicitacaoColetivaService.preencherPdf(solicitacao, responsavel);

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }

    @Test
    @DisplayName("Anexo III: Planejamento de Atividade de Campo")
    public void deveGerarAnexoIIIPlanejamentoAtividadeCampo() throws IOException {
        PlanejamentoAtividade planejamento = new PlanejamentoAtividade();
        planejamento.setCoordenadoresDaAtividade("Professor Responsável Teste");
        planejamento.setCoordenadoresDePesquisaExtensao("Coordenador de Extensão");
        planejamento.setDisciplina("Redes de Computadores");
        planejamento.setCurso("Análise e Desenvolvimento de Sistemas");
        planejamento.setTurma("P4");
        planejamento.setMetodologia("Visita guiada ao data center e análise de protocolos práticos.");
        planejamento.setObjetivos("Compreender a infraestrutura física de servidores e ativos de rede de alta disponibilidade.");
        planejamento.setCargaHorariaCompatibilidade("20 horas");
        planejamento.setJustificativaImportancia("Articulação dos conhecimentos teóricos abordados em sala de aula com a realidade do mercado.");
        planejamento.setNumeroParticipantes(25);
        planejamento.setItensSeguranca("Crachá de identificação, calçado fechado e normas de segurança do local.");
        planejamento.setCargaHorariaNoDiarioDeClasse("Compatível com a carga horária prática prevista.");
        planejamento.setContatoDosCoordenadores("(83) 97777-7777");

        Viagem viagem = new Viagem();
        Itinerario itinerario = new Itinerario(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 8, 20, 7, 0),
                LocalDateTime.of(2026, 8, 20, 18, 0),
                "Deslocamento e visita técnica ao Parque Tecnológico",
                "Campus Monteiro -> Parque Tecnológico",
                viagem
        );
        viagem.addItinerario(itinerario);
        planejamento.setViagem(viagem);

        byte[] pdfBytes = pdfPlanejamentoAtividade.preencherPdf(planejamento);

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }

    @Test
    @DisplayName("Anexo IV: Lista de Discentes Participantes")
    public void deveGerarAnexoIVListaDiscentesParticipantes() throws IOException {
        List<DiscenteParticipanteDTO> discentes = List.of(
                new DiscenteParticipanteDTO("Discente Participante Um", "20231001", "111.111.111-11", "Banco do Brasil", "1234", "01", "11111-1"),
                new DiscenteParticipanteDTO("Discente Participante Dois", "20231002", "222.222.222-22", "Caixa Econômica", "4321", "13", "22222-2"),
                new DiscenteParticipanteDTO("Discente Participante Três", "20231003", "333.333.333-33", "Nubank", "0001", "00", "33333-3")
        );

        byte[] pdfBytes = discentesParticipantesService.preencherAnexoIV(discentes, "Monteiro - PB");

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }

    @Test
    @DisplayName("Anexo V: Termo de Responsabilidade (Preenchido com dados do contexto coletivo)")
    public void deveGerarAnexoVTermoResponsabilidadeColetivo() throws IOException {
        UUID alunoId = UUID.randomUUID();
        UUID viagemId = UUID.randomUUID();

        User discenteMock = new User();
        discenteMock.setUserId(alunoId);
        discenteMock.setNome("Discente Coletivo Teste");
        discenteMock.setNumeroCpf("11122233344");
        discenteMock.setMatricula("20239999");
        discenteMock.setCurso("Análise e Desenvolvimento de Sistemas");
        discenteMock.setEmail("discente.coletivo@ifpb.edu.br");
        discenteMock.setTelefone("83966666666");
        discenteMock.setTurmaPeriodo("P4");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -21);
        discenteMock.setDataNascimento(cal.getTime());

        Viagem viagemMock = new Viagem();
        viagemMock.setId(viagemId);
        viagemMock.setDataPartida(LocalDate.of(2026, 8, 20));
        viagemMock.setDataRetorno(LocalDate.of(2026, 8, 22));
        Itinerario itinerario = new Itinerario(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 8, 20, 7, 0),
                LocalDateTime.of(2026, 8, 22, 18, 0),
                "Atividade de Campo - Visita Técnica",
                "Parque Tecnológico",
                viagemMock
        );
        viagemMock.addItinerario(itinerario);

        when(userRepository.findById(alunoId)).thenReturn(Optional.of(discenteMock));
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemMock));
        when(userRepository.findByMatricula(discenteMock.getMatricula())).thenReturn(Optional.of(discenteMock));

        byte[] pdfBytes = pdfTermoResponsabilidadeService.gerarTermoColetivaAdaptado(alunoId, viagemId, "Responsável Coletivo Teste", "83955555555");

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }

    @Test
    @DisplayName("Anexo VI: Relatório de Viagem Coletiva")
    public void deveGerarAnexoVIRelatorioViagemColetiva() throws IOException {
        RelatorioAtividade relatorio = new RelatorioAtividade();
        relatorio.setCoordenadoresDaAtividade("Professor Responsável Teste");
        relatorio.setDisciplinaOuProjeto("Redes de Computadores / Visita Técnica");
        relatorio.setRelatorio("A visita técnica transcorreu conforme o planejamento estabelecido. Os discentes participaram ativamente das explicações no centro de processamento de dados...");
        relatorio.setConsideracoesFinais("Os objetivos de aprendizagem foram plenamente atingidos, consolidando a ponte entre teoria e prática para os estudantes.");
        relatorio.setContatoDaInstituicao("contato@empresa.com.br / (83) 3333-3333");

        byte[] pdfBytes = pdfRelatorioAtividade.preencherPdf(relatorio);

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }
}
