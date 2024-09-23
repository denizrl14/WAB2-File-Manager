package wab.ad.filemanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Mono<Void> storeFile(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] fileBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(fileBytes);
                    DataBufferUtils.release(dataBuffer);
                    FileEntity fileEntity = new FileEntity(
                            filePart.filename(),
                            Objects.requireNonNull(filePart.headers().getContentType()).toString(),
                            fileBytes.length,
                            fileBytes);
                    return fileRepository.save(fileEntity);})
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

}