package wab.ad.filemanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // Store file using non-blocking I/O with DataBuffer
    public Mono<Void> storeFile(FilePart filePart) {
        return DataBufferUtils.join(filePart.content()) // Joins the DataBuffers into a single DataBuffer
                .flatMap(dataBuffer -> {
                    byte[] fileBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(fileBytes);
                    DataBufferUtils.release(dataBuffer); // Release buffer to prevent memory leaks
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    FileEntity fileEntity = new FileEntity(
                            filePart.filename(),
                            Objects.requireNonNull(filePart.headers().getContentType()).toString(),
                            fileBytes.length,
                            fileBytes);
                    log.info(">>> >>> FileEntity created: " + fileEntity.getFileName() + " >>> >>>");

                    return fileRepository.save(fileEntity)
                            .doOnSuccess(savedEntity -> log.info(">>> >>> File with id: " + savedEntity.getId()
                                    + " successfully stored in the database: " + savedEntity.getFileName() + " >>> >>>"));
                })
                .then();
    }

    // New method to store file content from String
    public Mono<Void> storeFileContent(String content) {
        return Mono.fromCallable(() -> {
                    byte[] fileBytes = content.getBytes(StandardCharsets.UTF_8);

                    // You can assign a default filename or extract it from the content if possible
                    String filename = "uploaded_text.txt";

                    Thread.sleep(1000);
                    // Create a new FileEntity and save to database
                    FileEntity fileEntity = new FileEntity(
                            filename,
                            MediaType.TEXT_PLAIN_VALUE,
                            fileBytes.length,
                            fileBytes);
                    log.info(">>> >>> FileEntity created from text content: " + fileEntity.getFileName() + " >>> >>>");

                    return fileEntity;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(fileEntity -> fileRepository.save(fileEntity)
                        .doOnSuccess(savedEntity -> log.info(">>> >>> Text content successfully stored in the database with id: "
                                + savedEntity.getId() + " >>> >>>"))
                )
                .then();
    }

    public Mono<ResponseEntity<byte[]>> loadFile(String id) {
        return getFileById(id)
                .map(fileEntity -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(fileEntity.getFileType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                        .body(fileEntity.getContent()));
    }

    public Mono<FileEntity> getFileById(String id) {
        return fileRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("File not found")));
    }

    public Flux<FileEntity> getAllFiles() {
        return fileRepository.findAll()
                .switchIfEmpty(Mono.error(new RuntimeException("No files found")));
    }
}