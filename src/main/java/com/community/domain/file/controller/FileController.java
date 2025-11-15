package com.community.domain.file.controller;

import com.community.domain.file.service.FileStorageService;
import com.community.domain.file.service.dto.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController implements FileApiSpec {

    private final FileStorageService fileStorageService;

    @Override
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable String fileId) {
        StoredFile file = fileStorageService.load(fileId);
        MediaType contentType = file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : (fileId + ".bin");
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8) // <= 핵심: UTF-8 filename*
                .build();

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(new ByteArrayResource(file.getContent()));
    }

    @Override
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable String fileId) {
        fileStorageService.delete("/files/" + fileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{fileId}")
    public ResponseEntity<String> upload(@PathVariable String fileId, @RequestParam("file") MultipartFile file) {
        String filePath = fileStorageService.saveManual(file, fileId);

        return ResponseEntity.ok(filePath);
    }
}
