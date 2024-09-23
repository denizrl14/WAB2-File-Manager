package wab.ad.filemanager;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void storeFile(MultipartFile file) throws IOException {
        try {
            FileEntity fileEntity = new FileEntity(file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getBytes());
            this.fileRepository.save(fileEntity);
        } catch (IOException e) {
            throw new IOException("Failed to store file: " + e.getMessage());
    }
    }

    public FileEntity getFileById(String id) throws IOException{
        return fileRepository.findById(id).orElseThrow(() -> new IOException("File not found"));
    }

}