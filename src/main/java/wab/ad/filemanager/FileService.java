package wab.ad.filemanager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");
    private final MeterRegistry meterRegistry;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    public FileService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    public Mono<Void> store(MultipartFile file) {
        Timer.Sample sample = Timer.start(meterRegistry);
        return Mono.fromRunnable(() -> {
            try {
                String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();


                try {
                    Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                meterRegistry.counter("fileManager_upload_bytes", "fileName", uniqueFilename)
                        .increment(file.getSize());
            } finally {
                sample.stop(meterRegistry.timer("fileManager_upload_timer", "fileSize", String.valueOf(file.getSize())));
            }
        });
    }
    public Mono<byte[]> loadFile(String filename) {
        Timer.Sample sample = Timer.start(meterRegistry);
        return Mono.fromCallable(() -> {
            try {
                Path file = rootLocation.resolve(filename);
                return Files.readAllBytes(file);
            } catch (IOException e) {
                throw new RuntimeException("File not found", e);
            }
        }).doFinally(signalType -> {
            sample.stop(meterRegistry.timer("fileManager_download_timer", "fileName", filename));
        });
    }
}