package wab.ad.filemanager;

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

    public void store(MultipartFile file) throws IOException {
        try {
            String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename));
        } catch (IOException e) {
            throw new IOException("Failed to store file: " + e.getMessage());
        }
    }

    public byte[] loadFile(String filename) throws IOException {
        try {
            Path file = rootLocation.resolve(filename);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new IOException("Failed to load file: " + e.getMessage());
        }
    }
}