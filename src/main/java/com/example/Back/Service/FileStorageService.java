// Em src/main/java/com/example/Back/Service/FileStorageService.java

package com.example.Back.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // Define o diretório onde as imagens serão salvas.
    // "user-photos" será uma pasta na raiz do seu projeto.
    private final Path root = Paths.get("user-photos");

    public FileStorageService() {
        try {
            // Cria o diretório se ele não existir
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível inicializar a pasta para upload!", e);
        }
    }

    public String save(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Falha ao armazenar arquivo vazio.");
            }
            // Cria um nome de arquivo único para evitar conflitos
            String uniqueName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Salva o arquivo no diretório de upload
            Files.copy(file.getInputStream(), this.root.resolve(uniqueName));

            return uniqueName; // Retorna o nome do arquivo salvo
        } catch (Exception e) {
            throw new RuntimeException("Falha ao armazenar o arquivo. Erro: " + e.getMessage());
        }
    }
}