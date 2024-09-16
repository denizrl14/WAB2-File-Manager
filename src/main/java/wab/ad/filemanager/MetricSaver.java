package wab.ad.filemanager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MetricSaver {

    public void saveMetricsToCSV(String timestamp, long fileSize, double processTime, double processCpuLoad, long processUsedMemoryString, String operation) {
        String csvFile = "metrics.csv";  // Pfad zur CSV-Datei
        boolean fileExists = Files.exists(Paths.get(csvFile));  // Überprüfen, ob die Datei existiert

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
            // Wenn die Datei noch nicht existiert, Kopfzeile hinzufügen
            if (!fileExists) {
                writer.write("Timestamp,FileSize,ProcessTime,ProcessCpuLoad,ProcessUsedMemory,Operation");
                writer.newLine();
            }

            // CSV-Daten schreiben (Zeitstempel, Dateigröße, Uploadzeit)
            writer.write(String.format("%s,%d,%.2f,%.2f,%d,%s", timestamp, fileSize, processTime, processCpuLoad, processUsedMemoryString, operation));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}