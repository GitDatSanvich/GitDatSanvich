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
    @Resource
    private BlockedThreadPool<String> blockedThreadPool;

    @GetMapping("/testPool")
    public void testPool() {
        //同步触发线程池
        List<Callable<String>> taskListSynchronous = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Callable<String> task = () -> {
                Random r = new Random();
                int sec = r.nextInt(20);
                Thread.sleep(sec * 1000);
                return "完成了，执行了" + sec + "秒，返回返回值。";
            };
            taskListSynchronous.add(task);
        }
        List<String> returnList = blockedThreadPool.submitAllSynchronous(taskListSynchronous);

        System.out.println(returnList);

        //异步触发线程池
        List<Runnable> taskListAsynchronous = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Runnable task = () -> {
                Random r = new Random();
                int sec = r.nextInt(20);
                try {
                    Thread.sleep(sec * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("完成了，执行了" + sec + "秒，返回返回值。");
            };
            taskListAsynchronous.add(task);
        }
        blockedThreadPool.submitAllAsynchronous(taskListAsynchronous);
    }


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
