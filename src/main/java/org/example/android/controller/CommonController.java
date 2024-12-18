package org.example.android.controller;


import org.example.android.common.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/common")
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

    @Value("${file.upload-dir}")
    private String basePath;

    // 允许上传的文件类型（所有图片类型）
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif"  // 支持JPEG, PNG 和 GIF 文件
    );

    // 最大文件大小 (以字节为单位, 例如: 10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final JdbcTemplate jdbcTemplate;

    public CommonController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 文件上传方法
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.error("文件为空");
        }

        // 检查文件类型
        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            return R.error("不支持的文件类型：" + file.getContentType());
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return R.error("文件过大，最大支持 " + MAX_FILE_SIZE / 1024 / 1024 + "MB");
        }

        // 获取并更新全局计数器
        Long counter = getAndIncrementGlobalCounter();
        // 构建新的文件名，格式为：1.jpg, 2.png 等
        String newName = counter.toString() + getFileExtension(file.getOriginalFilename());

        try {
            // 文件保存到预制路径
            File folder = new File(basePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file.transferTo(new File(folder, newName));

            // 返回文件名给前端用于后续请求
            return R.success(counter.toString()); // 只返回数字部分
        } catch (IOException e) {
            logger.error("文件上传失败：{}", e.getMessage(), e);
            return R.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 文件下载或回显方法
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            // 尝试找到与提供的名称匹配的文件，忽略扩展名
            File matchingFile = findMatchingFile(name);

            if (matchingFile == null || !matchingFile.exists()) {
                throw new FileNotFoundException("文件不存在");
            }

            response.setContentType(getContentType(matchingFile.getName())); // 设置正确的Content-Type
            response.setHeader("Content-Disposition", "inline; filename=" + matchingFile.getName()); // inline表示直接在浏览器中显示

            try (FileInputStream fis = new FileInputStream(matchingFile);
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (IOException e) {
            logger.error("文件下载失败：{}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败：" + e.getMessage());
        }
    }

    private File findMatchingFile(String baseName) {
        File folder = new File(basePath);
        File[] files = folder.listFiles((dir, name) -> name.startsWith(baseName));
        if (files != null && files.length > 0) {
            // 假设每个基础名只对应一个文件，取第一个匹配项
            return files[0];
        }
        return null;
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        } else {
            return "";
        }
    }

    private String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            default:
                return "application/octet-stream"; // 默认值
        }
    }

    private synchronized Long getAndIncrementGlobalCounter() {
        // 获取当前计数值
        Long currentCounter = jdbcTemplate.queryForObject("SELECT counter FROM global_image_counter WHERE id = 1", Long.class);
        if (currentCounter == null) {
            currentCounter = 0L;
        }

        // 更新计数器
        jdbcTemplate.update("UPDATE global_image_counter SET counter = ? WHERE id = 1", currentCounter + 1);

        return currentCounter + 1;
    }
}