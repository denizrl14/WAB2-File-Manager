package wab.ad.filemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public void store(MultipartFile file) throws IOException {
        Files.copy(file.getInputStream(), this.rootLocation.resolve(Objects.requireNonNull(file.getOriginalFilename())));

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFileType(file.getContentType());
        fileEntity.setSize(file.getSize());
        fileRepository.save(fileEntity);
    }

    public byte[] loadFile(String filename) throws IOException {
        Path file = rootLocation.resolve(filename);
        return Files.readAllBytes(file);
    }
}