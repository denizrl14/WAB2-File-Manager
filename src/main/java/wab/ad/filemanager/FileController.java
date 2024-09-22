package wab.ad.filemanager;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> handleFileUploadMultipart(@RequestPart("file") FilePart filePart) {
        return fileService.storeFile(filePart)
                .then(Mono.just(ResponseEntity.ok("File uploaded successfully: " + filePart.filename())))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500)
                        .body("Failed to upload file: " + e.getMessage())));
    }

    @PostMapping(value = "/upload", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public Mono<ResponseEntity<String>> handleFileUploadPlainText(@RequestBody String fileContent) {
        return fileService.storeFileContent(fileContent)
                .then(Mono.just(ResponseEntity.ok("File content uploaded successfully")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500)
                        .body("Failed to upload file content: " + e.getMessage())));
    }

    @GetMapping("/download/{id}")
    public Mono<ResponseEntity<byte[]>> downloadFile(@PathVariable Long id) {
        return fileService.loadFile(id)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(404)
                        .body(("Failed to download file: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))));
    }

    @GetMapping("/all")
    public Flux<FileEntity> getAllFiles() {
        return fileService.getAllFiles();
    }
}