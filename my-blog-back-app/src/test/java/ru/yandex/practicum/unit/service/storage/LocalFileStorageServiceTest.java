package ru.yandex.practicum.unit.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.exceptions.file.DirectoryNotFountException;
import ru.yandex.practicum.exceptions.file.FileNotFountException;
import ru.yandex.practicum.service.storage.LocalFileStorageService;
import ru.yandex.practicum.service.storage.name.UniqueFileNameService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private UniqueFileNameService uniqueFileNameService;
    private LocalFileStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(tempDir.resolve("test-bucket"));
        uniqueFileNameService = mock(UniqueFileNameService.class);
        service = new LocalFileStorageService(
                tempDir.toString(), "test-bucket", uniqueFileNameService);
    }

    @Nested
    class Constructor {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " "})
        void nullOrBlankRoot_throws(String root) {
            assertThatThrownBy(() -> new LocalFileStorageService(
                    root, "test-bucket", uniqueFileNameService))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Storage root directory cannot be null or blank");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "test/bucket", "test.bucket", "test bucket", "../test", "..", "."})
        void invalidBucketName_throws(String bucket) {
            assertThatThrownBy(() -> new LocalFileStorageService(
                    tempDir.toString(), bucket, uniqueFileNameService))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid directory name in config");
        }

        @Test
        void missingBucketDir_throws() {
            assertThatThrownBy(() -> new LocalFileStorageService(
                    tempDir.toString(), "nonexistent-bucket", uniqueFileNameService))
                    .isInstanceOf(DirectoryNotFountException.class)
                    .hasMessageContaining("nonexistent-bucket");
        }
    }


    @Nested
    class GetFile {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "  "})
        void invalidFilename_throws(String filename) {
            assertThatThrownBy(() -> service.getFile(filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid filename");
        }

        @Test
        void notExists_returnsEmpty() {
            assertThat(service.getFile("nonexistent.txt")).isEmpty();
        }

        @Test
        void exists_returnsContent() throws IOException {
            Path file = tempDir.resolve("test-bucket").resolve("image.png");
            Files.write(file, "content".getBytes());

            Optional<Resource> result = service.getFile("image.png");

            assertThat(result).isPresent();
            assertThat(result.get()).isInstanceOf(ByteArrayResource.class);
            ByteArrayResource resource = (ByteArrayResource) result.get();
            assertThat(resource.getByteArray()).isEqualTo("content".getBytes());
        }

        @Test
        void directoryInsteadOfFile_returnsEmpty() throws IOException {
            Files.createDirectories(tempDir.resolve("test-bucket").resolve("subdir"));

            assertThat(service.getFile("subdir")).isEmpty();
        }
    }


    @Nested
    class SaveFile {

        @Test
        void savesFile_returnsGeneratedName() throws IOException {
            when(uniqueFileNameService.generateFileName()).thenReturn("fixed-file-name");
            MockMultipartFile file = new MockMultipartFile(
                    "file", "image.png", "image/png", "content".getBytes());

            String result = service.saveFile(file);

            assertThat(result).isEqualTo("fixed-file-name");
            Path saved = tempDir.resolve("test-bucket").resolve("fixed-file-name");
            assertThat(saved).exists();
            assertThat(Files.readAllBytes(saved)).isEqualTo("content".getBytes());
        }

        @Test
        void transferFails_throwsUnchecked() throws IOException {
            when(uniqueFileNameService.generateFileName()).thenReturn("fixed-file-name");
            MultipartFile file = mock(MultipartFile.class);
            doThrow(new IOException("boom")).when(file).transferTo(any(Path.class));

            assertThatThrownBy(() -> service.saveFile(file))
                    .isInstanceOf(java.io.UncheckedIOException.class)
                    .hasMessageContaining("test-bucket")
                    .hasCauseInstanceOf(IOException.class);
        }
    }


    @Nested
    class DeleteFile {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " "})
        void invalidFilename_throws(String filename) {
            assertThatThrownBy(() -> service.deleteFile(filename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid filename");
        }

        @Test
        void notExists_throws() {
            assertThatThrownBy(() -> service.deleteFile("nonexistent.txt"))
                    .isInstanceOf(FileNotFountException.class)
                    .hasMessageContaining("test-bucket")
                    .hasMessageContaining("nonexistent.txt");
        }

        @Test
        void exists_deletesFile() throws IOException {
            Path file = tempDir.resolve("test-bucket").resolve("image.png");
            Files.write(file, "content".getBytes());

            service.deleteFile("image.png");

            assertThat(file).doesNotExist();
        }

        @Test
        void directoryInsteadOfFile_throws() throws IOException {
            Files.createDirectories(tempDir.resolve("test-bucket").resolve("subdir"));

            assertThatThrownBy(() -> service.deleteFile("subdir"))
                    .isInstanceOf(FileNotFountException.class);
        }
    }
}
