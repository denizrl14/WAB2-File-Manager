package wab.ad.filemanager;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");

    public Mono<Void> store(MultipartFile file) {
        return Mono.fromRunnable(() -> {
            try {
                String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename));
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }).subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public Mono<byte[]> loadFile(String filename) {
        return Mono.fromCallable(() -> {
                    try {
                        Path file = rootLocation.resolve(filename);
                        return Files.readAllBytes(file);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load file", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

}