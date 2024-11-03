package com.example.UserDocumentStorageService.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import com.example.UserDocumentStorageService.config.AwsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;


import java.io.*;

@Service
public class AwsS3Service {

    private AwsConfig awsConfig;


    private final AmazonS3 amazonS3;


    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;


    public AwsS3Service(@Lazy AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Bean
    public AmazonS3 s3Client() {
        BasicAWSCredentials basicAWSCredentials=new BasicAWSCredentials(accessKey,secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(region)
                .build();


    }

    public String uploadFile(MultipartFile multipartFile){

        try {
            File fileToSave = convertMultiPartToFile(multipartFile);
            String fileName = multipartFile.getOriginalFilename();
            amazonS3.putObject(bucketName,fileName,fileToSave);

        }catch (Exception ex){
            ex.getMessage();
        }
        return "File Uploaded...";
    }

    public String downloadFile(String fileName){
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {

            S3Object s3Object  = amazonS3.getObject(bucketName,fileName);

            inputStream = s3Object.getObjectContent();
            byte[] content = IOUtils.toByteArray(inputStream);

            String downloadDir = "/home/administrator/Projects/Backend-Assignment";
            File downloadedFile = new File(downloadDir, fileName);
            new File(downloadDir).mkdirs();

            fos = new FileOutputStream(downloadedFile);
            fos.write(content);


        } catch (Exception exception) {
           exception.getMessage();
           exception.printStackTrace();
        }

        return "File Downloaded...";
    }

    private File convertMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File convertFile = new File(multipartFile.getOriginalFilename());
        FileOutputStream fileOutputStream = new FileOutputStream(convertFile);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
        return convertFile;
    }

}

