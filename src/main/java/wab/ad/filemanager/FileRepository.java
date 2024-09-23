package wab.ad.filemanager;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface FileRepository extends ReactiveCrudRepository<FileEntity, String> {}
