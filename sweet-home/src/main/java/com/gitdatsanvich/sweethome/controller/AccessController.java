package com.gitdatsanvich.sweethome.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.gitdatsanvich.common.util.R;
import com.gitdatsanvich.sweethome.model.dto.AccessDTO;
import com.gitdatsanvich.sweethome.service.IpAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author TangChen
 * @date 2021/6/9 17:52
 */
@Slf4j
@RestController
@RequestMapping("/access")
public class AccessController {
    @Resource
    private IpAccessService ipAccessService;

    /**
     * 记录并确认是否可以访问
     *
     * @return access
     */
    @GetMapping()
    public R<AccessDTO> access(HttpServletRequest request) {
        // 这个一般是Nginx反向代理设置的参数

        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(StringPool.COMMA)) {
            String[] ipArray = ip.split(StringPool.COMMA);
            ip = ipArray[0];
        }
        String session = request.getSession().getId();
        log.info("当前请求IP为：{},当前请求Session为:{}.", ip, session);
        if (ip == null || StringPool.EMPTY.equals(ip)) {
            return R.ok(new AccessDTO(ip, 0, true, 0));
        }
        return R.ok(ipAccessService.access(ip));
    }
}
