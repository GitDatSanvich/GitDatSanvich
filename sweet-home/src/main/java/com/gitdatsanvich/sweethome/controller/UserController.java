package com.gitdatsanvich.sweethome.controller;

import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.common.util.BlockedThreadPool;
import com.gitdatsanvich.common.util.R;
import com.gitdatsanvich.sweethome.model.dto.RegisterDTO;
import com.gitdatsanvich.sweethome.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * @author TangChen
 * @date 2021/5/25 21:32
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    /**
     * 用户注册
     *
     * @return R
     */
    @PostMapping("/registered")
    public R<String> registered(@RequestBody RegisterDTO registerDTO) {
        try {
            userService.registered(registerDTO);
        } catch (BizException e) {
            R.failed(e.getMessage());
        }
        return R.ok();
    }
}
