package com.gitdatsanvich.sweethome.model.dto;

import lombok.Data;

/**
 * @author TangChen
 * @date 2021/5/25 22:58
 */
@Data
public class RegisterDTO {
    private String groupName;
    private String userName;
    private String password;
    private String name;
    private String email;
}
