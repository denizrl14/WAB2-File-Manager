package wab.ad.filemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");

    @Autowired
    private FileRepository fileRepository;

    public Mono<Void> store(MultipartFile file) {
        return Mono.fromRunnable(() -> {
            try {
                Files.copy(file.getInputStream(), this.rootLocation.resolve(Objects.requireNonNull(file.getOriginalFilename())));
                FileEntity fileEntity = new FileEntity();
                fileEntity.setFileName(file.getOriginalFilename());
                fileEntity.setFileType(file.getContentType());
                fileEntity.setSize(file.getSize());
                fileRepository.save(fileEntity);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file: " + e.getMessage());
            }
        });
    }
    public Mono<byte[]> loadFile(String filename) {
        return Mono.fromCallable(() -> {
            try {
                Path file = rootLocation.resolve(filename);
                return Files.readAllBytes(file);
            } catch (IOException e) {
                throw new RuntimeException("File not found", e);
            }
        });
    }
}