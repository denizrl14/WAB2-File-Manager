package wab.ad.filemanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        log.info("----- Receiving file: " + file.getOriginalFilename() + " -----");
        try {
            log.info("..... Processing file: " + file.getOriginalFilename() + " .....");
            Thread.sleep(1000);
            log.info(">>>>> Storing File: " + file.getOriginalFilename() + " >>>>>");
            fileService.storeFile(file);
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        log.info("----- Receiving Downloading-Request for file with id: " + id + " -----");
        try {
            FileEntity fileEntity = fileService.getFileById(id);
            log.info("..... Processing file: " + fileEntity.getFileName() + " .....");
            Thread.sleep(1000);
            byte[] fileContent = fileEntity.getContent();
            log.info("<<<<< Downloading file: " + fileEntity.getFileName() + " <<<<<");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(404).body(("Failed to download file: " + e.getMessage()).getBytes());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
