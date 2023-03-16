package com.gitdatsanvich.sweethome.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author TangChen
 * @date 2023/3/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO implements Serializable {
    private String message;
}
