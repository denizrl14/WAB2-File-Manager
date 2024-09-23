package wab.ad.filemanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


public interface FileRepository extends MongoRepository<FileEntity, String> {}
