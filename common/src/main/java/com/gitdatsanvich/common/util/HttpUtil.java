package com.gitdatsanvich.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author TangChen
 * @date 2021/6/7 11:14
 */
@Slf4j
public class HttpUtil {
    /**
     * HTTP POST 纯JSON
     *
     * @param url      url
     * @param JSONBody JSONBody
     * @return String
     */
    public static String sendHttpPost(String url, String JSONBody) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(JSONBody, StandardCharsets.UTF_8));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseContent = EntityUtils.toString(entity, "UTF-8");
            response.close();
            httpClient.close();
            return responseContent;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * '
     *
     * @param httpSendWay httpSendWay
     * @param key         key
     * @param url         url
     * @param JSONBody    JSONBody
     * @return String
     */
    public static String sendChatGPT(String httpSendWay, String key, String url, String JSONBody) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = null;
            if (HttpSendWay.POST.equals(httpSendWay)) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.addHeader("Authorization", "Bearer " + key);
                if (StringUtils.isNotEmpty(JSONBody)) {
                    httpPost.addHeader("Content-Type", "application/json");
                    httpPost.setEntity(new StringEntity(JSONBody, StandardCharsets.UTF_8));
                }
                response = httpClient.execute(httpPost);

            } else if (HttpSendWay.GET.equals(httpSendWay)) {
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader("Authorization", "Bearer " + key);
                response = httpClient.execute(httpGet);
            } else {
                return null;
            }
            HttpEntity entity = response.getEntity();
            String responseContent = EntityUtils.toString(entity, "UTF-8");
            response.close();
            httpClient.close();
            return responseContent;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
