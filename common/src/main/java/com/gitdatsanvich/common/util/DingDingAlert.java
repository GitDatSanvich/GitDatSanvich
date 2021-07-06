package com.gitdatsanvich.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author TangChen
 * @date 2021/6/29 17:13
 */
@Configuration
public class DingDingAlert {

    private static String webhook;

    /**
     * 推送警报
     */
    @Value("${dingding.webhook}")
    private void setWebhook(String webhook) {
        DingDingAlert.webhook = webhook;
    }

    public static void pushAlert(String title, String content) {
        String pushString = buildReqString(title, content);
        HttpUtil.sendHttpPost(webhook, pushString);
    }

    private static String buildReqString(String title, String content) {
        return "{\n" +
                "         \"msgtype\": \"markdown\",\n" +
                "         \"markdown\": {\n" +
                "             \"title\":\" " + title + "\",\n" +
                "             \"text\": \"" + "来自服务器的推送消息:" + content + "\"\n" +
                "         },\n" +
                "       \"at\": {" +
                "           \"isAtAll\": true " +
                "       }\n" +
                "    }";
    }
}
