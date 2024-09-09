package wab.ad.filemanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class FileManagerApplication implements CommandLineRunner {

    private final Path rootLocation = Paths.get("upload-dir");

    public static void main(String[] args) {
        SpringApplication.run(FileManagerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Files.createDirectories(rootLocation);
    }


}
