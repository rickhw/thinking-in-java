package com.gtcafe.asimov;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/encryption")
@Slf4j
public class EncryptionController {

    private final EncryptionService encryptionService;

    private static int counter = 0;

    public EncryptionController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFileOnly(@RequestParam("file") MultipartFile file) throws Exception {
        // Instant start = Instant.now();
        long t1 = System.currentTimeMillis();

        counter++;
        
        log.info("Received file: {}, size: [{}], time: [{}] counter: [{}]", file.getOriginalFilename(), file.getSize(), t1, counter);        

        // long maxFileSize = 1L * 1024 * 1024 * 1024; // 1 GiB
        // if (file.getSize() > maxFileSize) {
        //     return ResponseEntity.badRequest().body("File size exceeds the limit of 1 GiB");
        // }
        
        // File inputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
        // File outputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename() + ".enc");

        // file.transferTo(inputFile);

        long t2 = System.currentTimeMillis();

        return ResponseEntity.ok()
                .body("ok");
    }

    @PostMapping("/encrypt")
    public ResponseEntity<?> encryptFile(@RequestParam("file") MultipartFile file) throws Exception {
        // Instant start = Instant.now();
        long t1 = System.currentTimeMillis();
        counter++;
        
        log.info("Received file: {}, size: [{}], time: [{}] counter: [{}]", file.getOriginalFilename(), file.getSize(), t1, counter);        

        long maxFileSize = 1L * 1024 * 1024 * 1024; // 1 GiB
        if (file.getSize() > maxFileSize) {
            return ResponseEntity.badRequest().body("File size exceeds the limit of 1 GiB");
        }
        
        File inputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
        File outputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename() + ".enc");

        file.transferTo(inputFile);

        long t2 = System.currentTimeMillis();

        SecretKey secretKey = encryptionService.generateKey();
        encryptionService.encryptFile(inputFile, outputFile, secretKey);

        long t3 = System.currentTimeMillis();

        byte[] keyBytes = encryptionService.saveKey(secretKey);
        String keyBase64 = Base64.getEncoder().encodeToString(keyBytes);

        long t4 = System.currentTimeMillis();

        log.info("Receive file in {} ms", (t2 - t1));
        log.info("Encryption time in {} ms", (t3 - t2));
        log.info("Write file time in {} ms", (t4 - t3));

        return ResponseEntity.ok()
                .header("Encryption-Key", keyBase64)
                .body(Files.readAllBytes(outputFile.toPath()));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<?> decryptFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("key") String base64Key
    ) throws Exception {
        File inputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
        File outputFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename().replace(".enc", ".dec"));

        file.transferTo(inputFile);

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey secretKey = encryptionService.loadKey(keyBytes);
        encryptionService.decryptFile(inputFile, outputFile, secretKey);

        return ResponseEntity.ok(Files.readAllBytes(outputFile.toPath()));
    }
}
