package wab.ad.filemanager;

import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");

    @Autowired
    private FileRepository fileRepository;

    public Mono<Void> store(MultipartFile file) {
        return Mono.fromRunnable(() -> {
            try {
                String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename));
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        });
    }

    public Mono<byte[]> loadFile(String filename) {
        return Mono.fromCallable(() -> {
            try {
                Path file = rootLocation.resolve(filename);
                return Files.readAllBytes(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load file", e);
            }
        });
    }
}