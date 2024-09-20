package wab.ad.filemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public Mono<ResponseEntity<String>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        return fileService.store(file)
                .then(Mono.just(ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename())))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage())));
    }

    @GetMapping("/download/{id}")
    public Mono<ResponseEntity<byte[]>> downloadFile(@PathVariable Long id) {
        try {
            return fileService.getFileById(id)
                    .map(fileEntity -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                            .body(fileEntity.getContent()))
                    .onErrorResume(e -> Mono.just(ResponseEntity.status(404).body(("Failed to download file: " + Arrays.toString(e.getMessage().getBytes(StandardCharsets.UTF_8))).getBytes())));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(404).body(("Failed to download file: " + Arrays.toString(e.getMessage()
                    .getBytes(StandardCharsets.UTF_8))).getBytes()));
        }
    }

}
