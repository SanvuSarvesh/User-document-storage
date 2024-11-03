package com.example.UserDocumentStorageService;

import com.amazonaws.services.s3.AmazonS3;


import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.example.UserDocumentStorageService.service.AwsS3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AwsS3ServiceTest {


    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private AwsS3Service awsS3Service;

    private String bucketName = "test-bucket";
    private final String fileName = "test-file.png";
    private MockMultipartFile mockMultipartFile;


    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bucketName = bucketName;
        accessKey = "test-access-key";
        secretKey = "test-secret-key";
        region = "test-region";

        mockMultipartFile = new MockMultipartFile(
                fileName,
                fileName,
                "text/plain",
                "This is a test file".getBytes()
        );
    }

    @Test
    public void testUploadFile_Success() throws Exception {
        MultipartFile mockMultipartFile = Mockito.mock(MultipartFile.class);
        AwsS3Service awsS3Service = Mockito.mock(AwsS3Service.class);
        Mockito.lenient().when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        lenient().when(mockMultipartFile.getOriginalFilename()).thenReturn(fileName);
        lenient().when(awsS3Service.uploadFile(mockMultipartFile)).thenReturn("File Uploaded...");

        String result = awsS3Service.uploadFile(mockMultipartFile);

        assertEquals("File Uploaded...", result);

    }


    @Test
    public void testUploadFile_ExceptionHandling() throws Exception {
        AwsS3Service awsS3Service = Mockito.mock(AwsS3Service.class);
        when(awsS3Service.uploadFile(mockMultipartFile)).thenReturn("File conversion failed");
        //when(mockMultipartFile.getOriginalFilename()).thenThrow(new IOException("File conversion failed"));

        String result = awsS3Service.uploadFile(mockMultipartFile);

        assertEquals("File conversion failed", result);
        verify(amazonS3, times(0)).putObject(any(PutObjectRequest.class));
    }


    @Test
    public void testDownloadFile_ExceptionHandling() {

        AwsS3Service awsS3Service = Mockito.mock(AwsS3Service.class);
        lenient().when(awsS3Service.downloadFile(fileName)).thenReturn("File download failed");
        lenient().when(amazonS3.getObject(bucketName, fileName)).thenThrow(new RuntimeException("File download failed"));
        String result = awsS3Service.downloadFile(fileName);
        assertEquals("File download failed", result);

    }


    @Test
    void testDownloadFile() throws Exception {
        AwsS3Service awsS3Service = Mockito.mock(AwsS3Service.class);
        S3Object s3Object = Mockito.mock(S3Object.class);
        byte[] fileContent = "Sample file content".getBytes();
        InputStream byteArrayInputStream = new ByteArrayInputStream(fileContent);
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(byteArrayInputStream, null);

        lenient().when(amazonS3.getObject(bucketName, fileName)).thenReturn(s3Object);
        lenient().when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
        when(awsS3Service.downloadFile(fileName)).thenReturn("File Downloaded...");
        String result = awsS3Service.downloadFile(fileName);
        assertEquals("File Downloaded...", result);
        File downloadedFile = new File("/home/administrator/Projects/Backend-Assignment", fileName);
        if (downloadedFile.exists()) {
            downloadedFile.delete();
        }
    }

}
