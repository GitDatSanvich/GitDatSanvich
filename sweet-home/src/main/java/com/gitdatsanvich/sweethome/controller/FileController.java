package com.gitdatsanvich.sweethome.controller;

import com.gitdatsanvich.common.util.R;
import com.gitdatsanvich.sweethome.model.dto.FileResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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

        return R.ok();
    }
}
