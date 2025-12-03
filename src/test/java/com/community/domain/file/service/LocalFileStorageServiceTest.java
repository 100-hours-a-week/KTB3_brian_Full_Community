package com.community.domain.file.service;

import com.community.domain.file.service.dto.StoredFile;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileStorageServiceTest {

    private static final String HOST = "http://localhost";
    private LocalFileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "HOST", HOST);
    }

    @Test
    @DisplayName("정상 파일 저장 시 경로와 메타데이터를 저장한다.")
    void save() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "image/png",
                "test".getBytes(StandardCharsets.UTF_8)
        );

        //when
        String savedPath = fileStorageService.save(file);

        //then
        String fileId = savedPath.substring(savedPath.lastIndexOf('/') + 1);
        StoredFile storedFile = fileStorageService.load(fileId);
        assertThat(storedFile.getFilePath()).isEqualTo(savedPath);
        assertThat(storedFile.getOriginalFilename()).isEqualTo("sample.txt");
        assertThat(storedFile.getSize()).isEqualTo(file.getSize());
        assertThat(storedFile.getContent()).isEqualTo(file.getBytes());
    }

    @Test
    @DisplayName("비어 있는 파일 업로드 시 INVALID_FILE 예외를 던진다.")
    void save_throws_invalidFile_when_file_empty() {
        //given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        //when
        CustomException exception = assertThrows(CustomException.class, () -> fileStorageService.save(emptyFile));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE);
    }

    @Test
    @DisplayName("null 파일 업로드 시 INVALID_FILE 예외를 던진다.")
    void save_throws_invalidFile_when_file_null() {
        //when
        CustomException exception = assertThrows(CustomException.class, () -> fileStorageService.save(null));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE);
    }

    @Test
    @DisplayName("존재하지 않는 파일을 조회하면 FILE_NOT_FOUND 예외를 던진다.")
    void load_withUnknownFileId_throwsFileNotFound() {
        //when
        CustomException exception = assertThrows(CustomException.class, () -> fileStorageService.load("missing"));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("저장된 파일을 삭제하면 저장소에서 제거된다.")
    void delete_withExistingFile_removesFromStore() {
        //given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "delete-me.txt",
                "text/plain",
                "delete-me".getBytes(StandardCharsets.UTF_8)
        );
        String savedPath = fileStorageService.save(file);
        String fileId = savedPath.substring(savedPath.lastIndexOf('/') + 1);

        //when
        fileStorageService.delete(savedPath);

        //then
        CustomException exception = assertThrows(CustomException.class, () -> fileStorageService.load(fileId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("saveManual 은 지정한 식별자 그대로 파일을 저장한다.")
    void saveManual_withProvidedIdStoresFile() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "manual.txt",
                "text/plain",
                "manual-data".getBytes(StandardCharsets.UTF_8)
        );

        //when
        String savedPath = fileStorageService.saveManual(file, "manual-id");

        //then
        assertThat(savedPath).isEqualTo(HOST + "/files/manual-id");
        StoredFile storedFile = fileStorageService.load("manual-id");
        assertThat(storedFile.getOriginalFilename()).isEqualTo("manual.txt");
    }
}
