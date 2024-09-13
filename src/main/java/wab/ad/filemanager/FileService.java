package wab.ad.filemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("upload-dir");

    public void store(MultipartFile file) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename));
    }

    public byte[] loadFile(String filename) throws IOException {
        Path file = rootLocation.resolve(filename);
        return Files.readAllBytes(file);
    }
}