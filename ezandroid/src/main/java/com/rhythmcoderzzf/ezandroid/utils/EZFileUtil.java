package com.rhythmcoderzzf.ezandroid.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class EZFileUtil {

    private static final int BUFFER_SIZE = 8192;

    private EZFileUtil() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /*----- 基础文件操作 -----*/

    /**
     * 创建文件（包括父目录）
     *
     * @param filePath 文件路径
     * @return 是否创建成功
     */
    public static boolean createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) return false;
        createDir(file.getParent());
        return file.createNewFile();
    }

    /**
     * 递归创建目录
     *
     * @param dirPath 目录路径
     */
    public static void createDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("目录创建失败: " + dirPath);
        }
    }

    /**
     * 删除文件或目录（递归删除）
     *
     * @param path 目标路径
     */
    public static void delete(String path) {
        File target = new File(path);
        if (!target.exists()) return;

        if (target.isDirectory()) {
            File[] files = target.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file.getAbsolutePath());
                }
            }
        }
        if (!target.delete()) {
            throw new RuntimeException("删除失败: " + path);
        }
    }

    /*----- 字节流操作 -----*/

    /**
     * 字节流读取文件（适合二进制文件）
     *
     * @param filePath 文件路径
     * @return 文件内容的字节数组
     */
    public static byte[] readBytes(String filePath) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = bis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
    }

    /**
     * 字节流写入文件（自动创建目录）
     *
     * @param filePath 文件路径
     * @param data     字节数据
     * @param append   是否追加模式
     */
    public static void writeBytes(String filePath, byte[] data, boolean append) throws IOException {
        createDir(new File(filePath).getParent());
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath, append), BUFFER_SIZE)) {
            bos.write(data);
        }
    }

    /*----- 字符流操作 -----*/

    /**
     * 按行读取文本文件（指定编码）
     *
     * @param filePath 文件路径
     * @return 文本行列表
     */
    public static List<String> readLines(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    /**
     * 写入文本内容（默认UTF-8编码）
     *
     * @param filePath 文件路径
     * @param content  文本内容
     * @param append   是否追加模式
     */
    public static void writeText(String filePath, String content, boolean append) throws IOException {
        createDir(new File(filePath).getParent());
        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, append), StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }

    /*----- InputStream 处理 -----*/

    /**
     * 从输入流读取字节数据
     *
     * @param inputStream 输入流
     * @return 字节数组
     * @throws IOException 若读取失败
     */
    public static byte[] readBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }


    /**
     * 从输入流读取文本数据（指定编码）
     *
     * @param inputStream 输入流
     * @return 文本内容
     * @throws IOException 若读取失败
     */
    public static String readText(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
            return content.toString();
        }
    }

    /**
     * 将字符串写入输出流（覆盖或追加模式）,默认UTF-8
     *
     * @param outputStream 输出流
     * @param content      文本内容
     * @param append       是否追加写入
     * @throws IOException 若写入失败
     */
    public static void writeText(OutputStream outputStream, String content, boolean append) throws IOException {
        if (!append) {
            ((FileOutputStream) outputStream).getChannel().truncate(0); // 覆盖模式清空文件
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write(content);
            writer.flush();
        }
    }

    /**
     * 将输入流数据复制到输出流
     *
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @throws IOException 若复制失败
     */
    public static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        try (inputStream; outputStream) { // Java 9+ 语法：支持在try中使用已存在的变量
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }


    /**
     * 大文件流式处理（避免内存溢出）
     *
     * @param inputStream 输入流
     * @param outputPath  输出路径
     * @throws IOException 若处理失败
     */
    public static void copyStream(InputStream inputStream, String outputPath) throws IOException {
        try (inputStream; FileOutputStream fos = new FileOutputStream(outputPath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

    /*----- 高级文件操作 -----*/

    /**
     * 复制文件（覆盖目标文件）
     *
     * @param src  源文件路径
     * @param dest 目标文件路径
     */
    public static void copyFile(String src, String dest) throws IOException {
        Path destPath = new File(dest).toPath();
        createDir(destPath.getParent().toString());
        Files.copy(new File(src).toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 复制目录（递归复制）
     *
     * @param srcDir 源目录
     * @ destDir 目标目录
     */
    public static void copyDirectory(String srcDir, String destDir) throws IOException {
        File src = new File(srcDir);
        File[] files = src.listFiles();
        if (files == null) return;

        createDir(destDir);
        for (File file : files) {
            String targetPath = destDir + File.separator + file.getName();
            if (file.isDirectory()) {
                copyDirectory(file.getAbsolutePath(), targetPath);
            } else {
                copyFile(file.getAbsolutePath(), targetPath);
            }
        }
    }

    /**
     * 移动文件/目录
     *
     * @param src  源路径
     * @param dest 目标路径
     */
    public static void move(String src, String dest) throws IOException {
        Files.move(new File(src).toPath(), new File(dest).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /*----- 工具方法 -----*/

    /**
     * 获取文件大小（单位：字节）
     *
     * @param filePath 文件路径
     */
    public static long getFileSize(String filePath) {
        return new File(filePath).length();
    }

    /**
     * 检查文件是否存在
     *
     * @param path 路径
     */
    public static boolean exists(String path) {
        return new File(path).exists();
    }

    /*----- 扩展工具方法 -----*/

    /**
     * 计算文件MD5校验码（需流读取支持）
     *
     * @param filePath 文件路径
     * @return MD5字符串
     * @throws IOException 若读取失败
     */
    public static String getFileMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        try (InputStream is = new FileInputStream(filePath)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }
}
