package wab.ad.filemanager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");
    private final MeterRegistry meterRegistry;
    private final MetricSaver metricSaver = new MetricSaver();

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
            metricSaver.saveMetricsToCSV(formattedTimestamp, file.getSize(), uploadTime, processCpuLoad, processUsedMemory, "Upload");
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