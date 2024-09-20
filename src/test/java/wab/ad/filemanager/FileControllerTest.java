package wab.ad.filemanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    @Test
    void testFileUpload() throws IOException {
        // Arrange: Lade eine Datei aus dem Test-Resources-Ordner
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Test.txt");
        byte[] fileBytes = inputStream.readAllBytes();

        // Erstelle ein Mock-MultipartFile mit der geladenen Datei
        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-file.txt",
                "text/plain",
                fileBytes
        );

        // Act: Rufe die Methode zum Testen auf
        ResponseEntity<String> response = fileController.handleFileUpload(mockFile);

        // Assert: Überprüfe das Ergebnis
        assertEquals(200, response.getStatusCodeValue());  // 200 OK sollte zurückgegeben werden
        assertEquals("File uploaded successfully: test-file.txt", response.getBody());

        // Verify: Überprüfe, dass der FileService die Methode zum Speichern der Datei aufgerufen hat
        verify(fileService).storeFile(mockFile);
    }

    @Test
    void testDownloadFileSuccess() {
        // Arrange: Erstelle eine Mock FileEntity
        Long fileId = 1L;
        String fileName = "test-file.txt";
        byte[] fileContent = "This is a test file.".getBytes(StandardCharsets.UTF_8);

        FileEntity mockFileEntity = new FileEntity();
        mockFileEntity.setId(fileId);
        mockFileEntity.setFileName(fileName);
        mockFileEntity.setContent(fileContent);

        // Mock the fileService to return the mockFileEntity
        when(fileService.getFileById(fileId)).thenReturn(mockFileEntity);

        // Act: Rufe die Methode zum Testen auf
        ResponseEntity<byte[]> response = fileController.downloadFile(fileId);

        // Assert: Überprüfe das Ergebnis
        assertEquals(200, response.getStatusCodeValue());  // 200 OK sollte zurückgegeben werden
        assertArrayEquals(fileContent, response.getBody()); // Überprüfe den Dateiinhalt
        assertEquals("attachment; filename=\"" + fileName + "\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("application/octet-stream", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

        // Verify: Überprüfe, dass die getFileById-Methode aufgerufen wurde
        verify(fileService).getFileById(fileId);
    }

}