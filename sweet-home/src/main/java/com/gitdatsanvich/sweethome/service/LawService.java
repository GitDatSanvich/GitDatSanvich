package com.gitdatsanvich.sweethome.service;

import com.gitdatsanvich.common.exception.BizException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.List;

/**
 * @author TangChen
 * @date 2021/7/5 16:16
 */
public interface LawService {
    List<Map<String, String>> getCompanyInfo(String companyName) throws IOException, BizException;

    void getCompanyInfoBatch(List<String> nameList) throws IOException;
}
