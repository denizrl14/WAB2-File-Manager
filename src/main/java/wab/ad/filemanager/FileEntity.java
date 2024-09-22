package wab.ad.filemanager;

import jakarta.persistence.Lob;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table("file_entity")
public class FileEntity {

    @Id
    private Long id;

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

    public FileEntity() {}

}
