package com.gitdatsanvich.sweethome.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.common.util.R;
import com.gitdatsanvich.sweethome.service.LawService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/**
 * @author TangChen
 * @date 2021/7/5 16:14
 */

@Slf4j
@RestController
@RequestMapping("/law")
public class LawController {
    @Resource
    private LawService lawService;

    @GetMapping
    public R<List<Map<String, String>>> downLoad(@RequestParam("companyName") String companyName) throws IOException {
        log.info("获取公司信息:" + companyName);
        try {
            List<Map<String, String>> companyInfo = lawService.getCompanyInfo(companyName);
            return R.ok(companyInfo, "查询成功");
        } catch (BizException e) {
            return R.failed(e.getMessage() + " 原因可能为网页改版");
        }
    }

    @PostMapping
    public R<Boolean> downLoadBatch(@RequestBody List<String> nameList) throws IOException {
        System.out.println(nameList);
        lawService.getCompanyInfoBatch(nameList);
        return R.ok();
    }
}