package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.entity.TipoAfastamento;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para geração de documentos (PDFs) de Viagem Individual (Discentes)")
public class DocumentosViagemIndividualTest {

    @Mock
    private SolicitacaoIndividualRepository solicitacaoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViagemRepository viagemRepository;

    @InjectMocks
    private PdfSolicitacaoIndividualService pdfSolicitacaoIndividualService;

    @InjectMocks
    private PdfTermoResponsabilidadeService pdfTermoResponsabilidadeService;

    @Test
    @DisplayName("Anexo II: Solicitação Individual de Ajuda de Custo")
    public void deveGerarAnexoIISolicitacaoIndividualAjudaCusto() throws IOException {
        SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Date(),
                null,
                null,
                null,
                null,
                "Participação em Congresso Científico",
                new Date(),
                TipoAfastamento.MAIOR_08_HORAS,
                "Estudante Teste",
                "11122233344",
                "2023101010",
                "Análise e Desenvolvimento de Sistemas",
                "estudante@ifpb.edu.br",
                "83999999999",
                "Rua das Flores, 123",
                "Monteiro",
                "P4",
                "Apresentação de Artigo",
                "Recife - PE",
                "Pai Teste",
                "83988888888",
                "Banco do Brasil",
                "1234-5",
                "12345-6",
                true,
                true,
                true,
                true,
                true,
                "10/08/2026",
                "08:00",
                "12/08/2026",
                "18:00"
        );

        byte[] pdfBytes = pdfSolicitacaoIndividualService.preencherAnexoII(dto);

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }

    @Test
    @DisplayName("Anexo V: Termo de Responsabilidade (Preenchido com dados do contexto individual)")
    public void deveGerarAnexoVTermoResponsabilidadeIndividual() throws IOException {
        SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Date(),
                null,
                null,
                null,
                null,
                "Participação em Evento Esportivo",
                new Date(),
                TipoAfastamento.MAIOR_08_HORAS,
                "Estudante Esportista",
                "11122233344",
                "2023101010",
                "Análise e Desenvolvimento de Sistemas",
                "esportista@ifpb.edu.br",
                "83999999999",
                "Rua Principal, 45",
                "Campus Monteiro",
                "P4",
                "JOGOS DO IFPB",
                "João Pessoa - PB",
                "Responsável Teste",
                "83988888888",
                "Caixa",
                "0001",
                "99999-9",
                true,
                true,
                true,
                true,
                true,
                "15/08/2026",
                "07:00",
                "18/08/2026",
                "20:00"
        );

        User discenteMock = new User();
        discenteMock.setMatricula(dto.matricula());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        discenteMock.setDataNascimento(cal.getTime());

        when(userRepository.findByMatricula(dto.matricula())).thenReturn(Optional.of(discenteMock));

        byte[] pdfBytes = pdfTermoResponsabilidadeService.gerarPdfTermo(dto);

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }

    @Test
    @DisplayName("Anexo VII: Relatório de Viagem Individual (Prestação de contas pós-viagem)")
    public void deveGerarAnexoVIIRelatorioViagemIndividual() throws IOException {
        UUID solicitacaoId = UUID.randomUUID();
        RelatorioDiscenteDTO dto = new RelatorioDiscenteDTO(
                solicitacaoId,
                "Participação em palestras, minicursos e apresentação de trabalho científico no congresso.",
                new BigDecimal("150.00"),
                "Cento e cinquenta reais",
                new BigDecimal("80.00"),
                "Oitenta reais",
                "2",
                "Prestação de contas enviada dentro do prazo legal sem irregularidades."
        );

        SolicitacaoIndividual solicitacaoMock = new SolicitacaoIndividual();
        solicitacaoMock.setId(solicitacaoId);
        solicitacaoMock.setNome("Estudante Teste");
        solicitacaoMock.setCpf("11122233344");
        solicitacaoMock.setCurso("Análise e Desenvolvimento de Sistemas");
        solicitacaoMock.setMatricula("2023101010");
        solicitacaoMock.setTelefone("83999999999");
        solicitacaoMock.setEmail("estudante@ifpb.edu.br");
        solicitacaoMock.setLocalidadeEvento("Recife - PE");
        solicitacaoMock.setDataSaida("10/08/2026");
        solicitacaoMock.setHoraSaida("08:00");
        solicitacaoMock.setDataChegada("12/08/2026");
        solicitacaoMock.setHoraChegada("18:00");

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoMock));

        byte[] pdfBytes = pdfSolicitacaoIndividualService.preencherAnexoVII(dto);

        assertNotNull(pdfBytes, "O array de bytes do PDF não deve ser nulo");
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve estar vazio");
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"), "Os bytes gerados devem começar com a assinatura padrão de um PDF (%PDF-)");
    }
}
