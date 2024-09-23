package wab.ad.filemanager;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String fileName;

    private String fileType;

    private long size;

    @Lob
    private byte[] content;

    public FileEntity(String fileName, String fileType, long size, byte[] content) {
        this.fileName = UUID.randomUUID() + "_" + fileName;
        this.fileType = fileType;
        this.size = size;
        this.content = content;
    }
}
