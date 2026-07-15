package br.edu.ifpb.ads.acvd.controller;

import br.edu.ifpb.ads.acvd.dto.SolicitacaoIndividualDTO;
import br.edu.ifpb.ads.acvd.service.PdfSolicitacaoIndividualService;
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

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PdfSolicitacaoIndividualController.class) // <--- Coloque o nome exato do seu Controller aqui
@AutoConfigureMockMvc(addFilters = false)
public class PdfSolicitacaoIndividualControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PdfSolicitacaoIndividualService pdfService;

    @Test
    @WithMockUser
    public void TI03_deveGerarAnexoIIDeViagemIndividualParaDownload() throws Exception {
        SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(
                null, null, null, null, null, null, null,
                "Visita Técnica", new Date(), null, "Josias", "00011122233", "12345",
                "ADS", "josias@academico.ifpb.edu.br", "9999", "Rua X", "Monteiro", "P4",
                "Evento", "João Pessoa", "Contato", "Conta 1", "Banco", "Ag", "Corrente",
                true, false, false, false, false, "2023-10-10", "08:00", "2023-10-12", "18:00"
        );

        byte[] pdfFakeBytes = "%PDF-1.4 Conteudo Fake Anexo II".getBytes();
        when(pdfService.preencherAnexoII(any(SolicitacaoIndividualDTO.class))).thenReturn(pdfFakeBytes);

        mockMvc.perform(post("/api/pdf/gerar-solicitacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(pdfFakeBytes));
    }

    @Test
    @WithMockUser
    public void TI03B_deveRetornar400AoEnviarJsonVazioNaRequisicaoPost() throws Exception {
        when(pdfService.preencherAnexoII(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON vazio ou com campos obrigatórios ausentes."));

        mockMvc.perform(post("/api/pdf/gerar-solicitacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void TI03C_deveRetornar500QuandoPdfServiceLancarRuntimeException() throws Exception {
        SolicitacaoIndividualDTO dto = new SolicitacaoIndividualDTO(
                null, null, null, null, null, null, null,
                "Visita Técnica", new Date(), null, "Josias", "00011122233", "12345",
                "ADS", "josias@academico.ifpb.edu.br", "9999", "Rua X", "Monteiro", "P4",
                "Evento", "João Pessoa", "Contato", "Conta 1", "Banco", "Ag", "Corrente",
                true, false, false, false, false, "2023-10-10", "08:00", "2023-10-12", "18:00"
        );

        when(pdfService.preencherAnexoII(any(SolicitacaoIndividualDTO.class)))
                .thenThrow(new RuntimeException("Erro genérico: falha ao ler o template do PDF Anexo II."));

        mockMvc.perform(post("/api/pdf/gerar-solicitacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }
}