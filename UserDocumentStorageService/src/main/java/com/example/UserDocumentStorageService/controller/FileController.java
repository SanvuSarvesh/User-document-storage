package com.example.UserDocumentStorageService.controller;

import com.example.UserDocumentStorageService.service.AwsS3Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/files")
public class FileController {

    private final AwsS3Service s3Service;

    @Autowired
    public FileController(AwsS3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile multipartFile){
        String response = s3Service.uploadFile(multipartFile);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/download/{file}")
    public ResponseEntity<String> download(@PathVariable("file") String file) {
        String response = s3Service.downloadFile(file);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}

