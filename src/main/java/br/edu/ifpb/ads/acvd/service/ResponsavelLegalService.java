package br.edu.ifpb.ads.acvd.service;

import br.edu.ifpb.ads.acvd.dto.ResponsavelLegalDTO;
import br.edu.ifpb.ads.acvd.entity.Documento;
import br.edu.ifpb.ads.acvd.entity.ResponsavelLegal;
import br.edu.ifpb.ads.acvd.entity.User;
import br.edu.ifpb.ads.acvd.repository.ResponsavelLegalRepository;
import br.edu.ifpb.ads.acvd.repository.UserRepository;
import br.edu.ifpb.ads.acvd.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResponsavelLegalService {

    private final ResponsavelLegalRepository responsavelLegalRepository;
    private final UserRepository userRepository;
    private final DocumentoService documentoService;
    private final DocumentoRepository documentoRepository;

    public ResponsavelLegalDTO obter(UUID userId) {
        ResponsavelLegal rl = responsavelLegalRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Responsável legal não cadastrado."));
        return new ResponsavelLegalDTO(rl);
    }

    @Transactional
    public ResponsavelLegalDTO atualizarDados(UUID userId, ResponsavelLegalDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ResponsavelLegal rl = responsavelLegalRepository.findByUserUserId(userId)
                .orElse(new ResponsavelLegal());

        rl.setNome(dto.nome());
        rl.setCpf(dto.cpf());
        rl.setRg(dto.rg());
        rl.setContato(dto.contato());
        rl.setUser(user);

        return new ResponsavelLegalDTO(responsavelLegalRepository.save(rl));
    }

    @Transactional
    public void uploadDocumentoIdentificacao(UUID userId, MultipartFile file) {
        ResponsavelLegal rl = responsavelLegalRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cadastre os dados do responsável primeiro."));

        // Limpeza de documento anterior
        if (rl.getDocumentoIdentificacao() != null) {
            Documento antigo = rl.getDocumentoIdentificacao();
            documentoService.removerDocumentoFisico(antigo);
            rl.setDocumentoIdentificacao(null);
            documentoRepository.delete(antigo);
            documentoRepository.flush();
        }

        Documento novoDoc = documentoService.processarUpload(file);
        novoDoc.setResponsavelLegal(rl);
        rl.setDocumentoIdentificacao(novoDoc);

        responsavelLegalRepository.save(rl);
    }
}