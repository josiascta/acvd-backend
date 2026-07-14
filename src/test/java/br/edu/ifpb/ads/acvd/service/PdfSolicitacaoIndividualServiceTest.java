package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.entity.SolicitacaoIndividual;
import br.edu.ifpb.ads.acvd.repository.SolicitacaoIndividualRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PdfSolicitacaoIndividualServiceTest {

    @Mock
    private SolicitacaoIndividualRepository solicitacaoRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PdfSolicitacaoIndividualService pdfService;

    @Test
    public void devePreencherAnexoIICorretamente() throws IOException {
        SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(
                null, null, null, null, null, null, null,
                "Evento Teste", new Date(), null, "Josias", "00011122233", "12345",
                "ADS", "josias@ifpb.edu.br", "9999", "Rua X", "Monteiro", "P4",
                "Visita", "João Pessoa", "Contato", "Conta 1", "Banco", "Ag", "Corrente",
                true, false, false, false, false, "2023-10-10", "08:00", "2023-10-12", "18:00"
        );

        byte[] pdfBytes = pdfService.preencherAnexoII(dto);
        assertTrue(pdfBytes.length > 0);
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF-"));
    }

    @Test
    public void devePreencherAnexoVIICorretamente() throws IOException {
        UUID id = UUID.randomUUID();
        RelatorioDiscenteDTO dto = new RelatorioDiscenteDTO(id, "Atividades", new BigDecimal("100"), "Cem", new BigDecimal("50"), "Cinquenta", "1", "Obs");

        SolicitacaoIndividual solicitacaoMock = new SolicitacaoIndividual();
        solicitacaoMock.setNome("Aluno Teste");

        when(solicitacaoRepository.findById(id)).thenReturn(Optional.of(solicitacaoMock));

        byte[] pdfBytes = pdfService.preencherAnexoVII(dto);
        assertTrue(pdfBytes.length > 0);
    }
}