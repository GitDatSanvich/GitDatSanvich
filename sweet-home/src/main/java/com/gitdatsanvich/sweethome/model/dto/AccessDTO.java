package com.gitdatsanvich.sweethome.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author TangChen
 * @date 2021/6/9 17:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessDTO {
    private Integer accessTime;
    private Boolean accessAble;
}
