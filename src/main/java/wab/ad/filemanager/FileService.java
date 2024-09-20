package wab.ad.filemanager;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.IOException;

@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Mono<Void> store(MultipartFile file) {
        return Mono.fromRunnable(() -> {
            try {
                FileEntity fileEntity = new FileEntity(file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getBytes());
                this.fileRepository.save(fileEntity);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }).subscribeOn(Schedulers.boundedElastic())
                .then();
    }
//
//    public Mono<byte[]> loadFile(String filename) {
//        return Mono.fromCallable(() -> {
//                    try {
//                        Path file = rootLocation.resolve(filename);
//                        return Files.readAllBytes(file);
//                    } catch (IOException e) {
//                        throw new RuntimeException("Failed to load file", e);
//                    }
//                })
//                .subscribeOn(Schedulers.boundedElastic());
//    }

    public Mono<FileEntity> getFileById(Long id) {
        return Mono.fromCallable(() -> fileRepository.findById(id).orElseThrow())
                .subscribeOn(Schedulers.boundedElastic());
    }

}