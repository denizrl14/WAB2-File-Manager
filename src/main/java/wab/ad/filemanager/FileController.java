package wab.ad.filemanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> handleFileUploadMultipart(@RequestPart("file") FilePart filePart) throws InterruptedException {
        log.info("----- Receiving file: " + filePart.filename() + " -----");
        log.info("..... Processing file: " + filePart.filename() + " .....");
        Thread.sleep(1000);
        log.info("----- Uploading file: " + filePart.filename() + " -----");
        return fileService.storeFile(filePart)
                .then(Mono.just(ResponseEntity.ok("File uploaded successfully: " + filePart.filename())))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500)
                        .body("Failed to upload file: " + e.getMessage())));
    }

    @GetMapping("/download/{id}")
    public Mono<ResponseEntity<byte[]>> downloadFile(@PathVariable String id) throws InterruptedException {
        log.info("----- Receiving Download-Request for file with id: " + id + " -----");
        log.info("..... Processing file: " + id + " .....");
        Thread.sleep(1000);
        log.info("<<<<< Downloading file with id: " + id + " <<<<<");
        return fileService.loadFile(id)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(404)
                        .body(("Failed to download file: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))));
    }

}