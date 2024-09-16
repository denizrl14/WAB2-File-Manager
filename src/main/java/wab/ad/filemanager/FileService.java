package wab.ad.filemanager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");
    private final MeterRegistry meterRegistry;

    @Autowired
    public FileService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    public void store(MultipartFile file) throws IOException {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {

            String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();


            Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename));


            meterRegistry.counter("fileManager_upload_bytes", "fileName", uniqueFilename)
                    .increment(file.getSize());
        } finally {

            sample.stop(meterRegistry.timer("fileManager_upload_timer", "fileSize", String.valueOf(file.getSize())));
        }
    }

    public byte[] loadFile(String filename) throws IOException {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Path file = rootLocation.resolve(filename);
            byte[] fileContent = Files.readAllBytes(file);

            meterRegistry.counter("fileManager_download_bytes", "fileName", filename)
                    .increment(fileContent.length);

            return fileContent;
        } finally {

            sample.stop(meterRegistry.timer("fileManager_download_timer", "fileName", filename));
        }
    }
}