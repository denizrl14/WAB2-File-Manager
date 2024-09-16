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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");
    private final MeterRegistry meterRegistry;
    private final MetricSaver metricSaver = new MetricSaver();

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
                Runtime runtime = Runtime.getRuntime();
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

                // Gesamter Speicher, der der JVM zugewiesen wurde (in Bytes)
                long totalMemory = runtime.totalMemory();
                // Freier Speicher innerhalb der JVM (in Bytes)
                long freeMemory = runtime.freeMemory();
                // Maximaler Speicher, den die JVM verwenden kann (abhängig von den JVM-Optionen) (in Bytes)
                long maxMemory = runtime.maxMemory();
                // Verwendeter Speicher (berechnet als totalMemory - freeMemory)
                long processUsedMemory = totalMemory - freeMemory;

                double processCpuLoad = osBean.getProcessCpuLoad() * 100;

                long uploadTime = sample.stop(meterRegistry.timer("fileManager_upload_timer", "fileSize", String.valueOf(file.getSize())));

                LocalDateTime timestamp = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTimestamp = timestamp.format(formatter);

                // Upload-Zeit in Nanosekunden stoppen

                // Daten in CSV speichern
                metricSaver.saveMetricsToCSV(formattedTimestamp, file.getSize(), uploadTime, processCpuLoad, processUsedMemory, "Upload");            }
        });
    }
    public Mono<byte[]> loadFile(String filename) {
        Timer.Sample sample = Timer.start(meterRegistry);
        return Mono.fromCallable(() -> {
            byte[] fileContent = null;
            try {
                Path file = rootLocation.resolve(filename);
                fileContent = Files.readAllBytes(file);

                meterRegistry.counter("fileManager_download_bytes", "fileName", filename)
                        .increment(fileContent.length);

                return fileContent;
            } finally {
                Runtime runtime = Runtime.getRuntime();
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

                // Gesamter Speicher, der der JVM zugewiesen wurde (in Bytes)
                long totalMemory = runtime.totalMemory();
                // Freier Speicher innerhalb der JVM (in Bytes)
                long freeMemory = runtime.freeMemory();
                // Verwendeter Speicher (berechnet als totalMemory - freeMemory)
                long processUsedMemory = totalMemory - freeMemory;

                // CPU-Auslastung für den Prozess in Prozent
                double processCpuLoad = osBean.getProcessCpuLoad() * 100;

                // Stoppe den Timer und erhalte die Download-Zeit in Nanosekunden
                long downloadTime = sample.stop(meterRegistry.timer("fileManager_download_timer", "fileName", filename));

                // Zeitstempel für den CSV-Eintrag
                LocalDateTime timestamp = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTimestamp = timestamp.format(formatter);

                // Daten in CSV speichern
                metricSaver.saveMetricsToCSV(formattedTimestamp, fileContent.length, downloadTime, processCpuLoad, processUsedMemory, "Download");
            }
        });
    }
}