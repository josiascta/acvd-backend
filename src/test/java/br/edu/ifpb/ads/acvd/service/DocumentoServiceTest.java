package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.entity.Documento;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.DocumentoRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private UserRepository userRepository;

    @TempDir
    Path pastaUploadTemporaria;

    private DocumentoService documentoService;

    @BeforeEach
    public void setup() {
        documentoService = new DocumentoService(documentoRepository, userRepository, pastaUploadTemporaria.toString());
    }

    @Test
    public void deveSalvarDocumentoESubstituirOAntigoFisicamente() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        MockMultipartFile fileMock = new MockMultipartFile("file", "cnh.pdf", "application/pdf", "Conteúdo Fake CNH".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));

        Documento docSalvo = documentoService.salvarDocumentoDoUsuario(userId, fileMock);

        assertNotNull(docSalvo);
        assertEquals("cnh.pdf", docSalvo.getNomeOriginal());
        assertNotNull(docSalvo.getHash());

        Path caminhoFisico = Path.of(docSalvo.getCaminhoDoArquivo());
        assertTrue(Files.exists(caminhoFisico), "O arquivo deve ter sido escrito no disco temporário");
    }
}