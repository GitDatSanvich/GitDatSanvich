package com.gitdatsanvich.sweethome.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.common.util.R;
import com.gitdatsanvich.sweethome.model.dto.FileResponseDTO;
import com.gitdatsanvich.sweethome.util.FileHeaderCheckUtil;
import com.gitdatsanvich.sweethome.util.StorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author TangChen
 * @date 2021/5/28 13:52
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    /**
     * 多文件上传
     *
     * @param fileList      fileList
     * @param uploadType    uploadType 文件类型定义
     * @param needThumbnail needThumbnail 是否需要缩略图
     * @return fileInfo
     */
    @PostMapping("/upload")
    public R<List<FileResponseDTO>> uploadFileList(@RequestParam("fileList") List<MultipartFile> fileList,
                                                   @RequestParam(name = "uploadType", required = false) String uploadType,
                                                   @RequestParam(name = "needThumbnail", required = false) Boolean needThumbnail) {
        List<FileResponseDTO> fileResponseList = new ArrayList<>();
        try {
            if (fileList == null || fileList.size() == 0) {
                throw BizException.FILE_EXCEPTION.newInstance("文件为空上传失败");
            }
            if (needThumbnail == null) {
                needThumbnail = false;
            }
            for (MultipartFile file : fileList) {
                /*文件生成唯一UUID*/
                String uuid = UUID.randomUUID().toString().replaceAll(StringPool.DASH, StringPool.EMPTY);
                FileResponseDTO fileResponseDTO = new FileResponseDTO();
                /*文件格式校验*/
                String type = FileHeaderCheckUtil.checkFileHeader(file, uploadType);
                /*文件保存*/
                ByteArrayOutputStream outputStream = StorageUtil.cloneInputStream(file.getInputStream());
                // 打开两个新的输入流
                InputStream streamForThumbnail = new ByteArrayInputStream(outputStream.toByteArray());
                InputStream streamForSave = new ByteArrayInputStream(outputStream.toByteArray());
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || StringPool.EMPTY.equals(originalFilename)) {
                    throw BizException.FILE_EXCEPTION.newInstance("文件名为空");
                }
                int num = originalFilename.lastIndexOf(StringPool.DOT);
                if (-1 == num) {
                    throw BizException.FILE_EXCEPTION.newInstance("文件后缀获取失败");
                }
                String suffix = originalFilename.substring(num + 1);
                String url = StorageUtil.save(streamForSave, suffix, uuid);
                String thumbnail = null;
                /*是否需要缩略图 是 执行*/
                if (needThumbnail) {
                    thumbnail = StorageUtil.saveThumbnail(suffix, streamForThumbnail, type, uuid);
                }
                fileResponseDTO.setFileName(originalFilename);
                fileResponseDTO.setThumbnail(thumbnail);
                fileResponseDTO.setUrl(url);
                fileResponseDTO.setFileType(type);
                fileResponseList.add(fileResponseDTO);
            }
        } catch (Exception e) {
            log.error("文件上传失败" + e.getMessage());
        }
        return R.ok(fileResponseList);
    }
}
