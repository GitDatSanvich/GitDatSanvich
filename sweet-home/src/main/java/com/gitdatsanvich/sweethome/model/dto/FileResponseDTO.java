package com.gitdatsanvich.sweethome.model.dto;

import lombok.Data;

/**
 * @author TangChen
 * @date 2021/5/28 13:56
 */
@Data
public class FileResponseDTO {
    private String url;
    private String fileType;
    private String thumbnail;
    private String fileName;
}
