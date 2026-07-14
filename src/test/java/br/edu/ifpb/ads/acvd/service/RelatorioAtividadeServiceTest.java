package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.RelatorioAtividadeDTO;
import br.edu.ifpb.ads.acvd.entity.RelatorioAtividade;
import br.edu.ifpb.ads.acvd.entity.Viagem;
import br.edu.ifpb.ads.acvd.repository.RelatorioAtividadeRepository;
import br.edu.ifpb.ads.acvd.repository.ViagemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RelatorioAtividadeServiceTest {

    @Mock
    private RelatorioAtividadeRepository relatorioRepository;
    @Mock
    private ViagemRepository viagemRepository;
    @Mock
    private PdfRelatorioAtividade pdfService;

    @TempDir
    Path pastaUploadTemporaria;

    private RelatorioAtividadeService relatorioAtividadeService;

    @BeforeEach
    public void setup() {
        relatorioAtividadeService = new RelatorioAtividadeService(
                relatorioRepository, viagemRepository, pdfService, pastaUploadTemporaria.toString());
    }

    @Test
    public void deveProcessarRelatorioGerarPdfESalvarNoDisco() throws IOException {
        UUID userId = UUID.randomUUID();
        UUID viagemId = UUID.randomUUID();

        RelatorioAtividadeDTO dto = new RelatorioAtividadeDTO(
                null,              // id
                "Coord",           // coordenadoresDaAtividade
                "Disciplina",      // disciplinaOuProjeto
                "Texto Relatório", // relatorio
                "Fim",             // consideracoesFinais
                "Contato",         // contatoDaInstituicao
                viagemId           // viagemId
        );

        Viagem viagemMock = new Viagem();
        viagemMock.setId(viagemId);

        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemMock));
        when(relatorioRepository.findByViagemId(viagemId)).thenReturn(Optional.empty());
        when(relatorioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        byte[] pdfFalso = "%PDF-Falso".getBytes();
        when(pdfService.preencherPdf(any(RelatorioAtividade.class))).thenReturn(pdfFalso);

        // ACT (Ação)
        RelatorioAtividadeDTO resultado = relatorioAtividadeService.processarRelatorio(userId, dto);

        assertNotNull(resultado, "O resultado não deveria ser nulo");
        assertEquals("Coord", resultado.coordenadoresDaAtividade(), "Os dados do DTO devem ser preservados");

        File[] arquivosSalvos = pastaUploadTemporaria.toFile().listFiles();
        assertNotNull(arquivosSalvos);

        assertEquals(1, arquivosSalvos.length, "O arquivo PDF deveria ter sido salvo na pasta física temporária");
    }
}