package com.citywalk.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Tag(name = "上传", description = "图片上传")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Value("${citywalk.upload.dir}")
    private String uploadDir;

    @Operation(summary = "上传图片，返回访问 URL")
    @PostMapping
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("不支持的图片格式：" + ext);
        }

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());

        Map<String, Object> result = new HashMap<>();
        result.put("url", "/uploads/" + filename);
        return result;
    }
}
