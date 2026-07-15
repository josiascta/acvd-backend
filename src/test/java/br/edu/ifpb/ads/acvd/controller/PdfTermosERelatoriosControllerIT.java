package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.RelatorioDiscenteDTO;
import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.service.PdfSolicitacaoIndividualService;
import br.edu.ifpb.ads.acvd.service.PdfTermoResponsabilidadeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.ifpb.ads.acvd.service.RelatorioDiscenteService;

@WebMvcTest(controllers = {TermoResponsabilidadeController.class, RelatorioDiscenteController.class})
@AutoConfigureMockMvc(addFilters = false)
public class PdfTermosERelatoriosControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PdfTermoResponsabilidadeService termoService;

    @MockitoBean
    private PdfSolicitacaoIndividualService relatorioService;

    @MockitoBean
    private RelatorioDiscenteService relatorioDiscenteService;

    @Test
    @WithMockUser
    public void TI04_deveGerarAnexoVComFluxoDeDadosParaDownload() throws Exception {
        SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(
                null, null, null, null, null, null, null,
                "Justificativa", new Date(), null, "Aluno Maior de Idade", "00011122233", "12345",
                "ADS", "aluno@ifpb.edu.br", "9999", "Rua X", "Monteiro", "P4",
                "Visita Técnica", "João Pessoa", "Contato", "Conta 1", "Banco", "Ag", "Corrente",
                true, false, false, false, false, "2023-10-10", "08:00", "2023-10-12", "18:00"
        );

        byte[] pdfFakeBytes = "%PDF-1.4 Termo de Compromisso".getBytes();
        when(termoService.gerarPdfTermo(any(SolicitacaoIndividualDTO.class))).thenReturn(pdfFakeBytes);

        mockMvc.perform(post("/api/pdf/termo-responsabilidade/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(pdfFakeBytes));
    }

    @Test
    @WithMockUser
    public void TI05_deveGerarRelatorioAnexoVIIDeViagem() throws Exception {
        RelatorioDiscenteDTO dto = new RelatorioDiscenteDTO(
                UUID.randomUUID(), "Histórico de Atividades e Locais",
                new BigDecimal("150.00"), "Cento e cinquenta",
                new BigDecimal("0.00"), "Zero", "0", "Ocorreu tudo bem"
        );

        byte[] pdfFakeBytes = "%PDF-1.4 Relatorio de Viagem".getBytes();
        when(relatorioService.preencherAnexoVII(any(RelatorioDiscenteDTO.class))).thenReturn(pdfFakeBytes);

        mockMvc.perform(post("/api/relatorios-discentes/gerar-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(pdfFakeBytes));
    }

    @Test
    @WithMockUser
    public void TI04B_deveRetornar400AoEnviarCorpoInvalidoParaAnexoV() throws Exception {
        when(termoService.gerarPdfTermo(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corpo de requisição inválido ou vazio para Anexo V."));

        mockMvc.perform(post("/api/pdf/termo-responsabilidade/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void TI05B_deveRetornar404AoLancarResponseStatusExceptionNoAnexoVII() throws Exception {
        RelatorioDiscenteDTO dto = new RelatorioDiscenteDTO(
                UUID.randomUUID(), "Histórico de Atividades e Locais",
                new BigDecimal("150.00"), "Cento e cinquenta",
                new BigDecimal("0.00"), "Zero", "0", "Ocorreu tudo bem"
        );

        when(relatorioService.preencherAnexoVII(any(RelatorioDiscenteDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem ou solicitação não encontrada no banco de dados."));

        mockMvc.perform(post("/api/relatorios-discentes/gerar-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}