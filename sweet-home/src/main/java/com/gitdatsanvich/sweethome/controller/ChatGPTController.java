package com.gitdatsanvich.sweethome.controller;

import cn.hutool.json.JSONUtil;
import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.common.util.DingDingAlert;
import com.gitdatsanvich.common.util.HttpSendWay;
import com.gitdatsanvich.common.util.HttpUtil;
import com.gitdatsanvich.common.util.R;
import com.gitdatsanvich.sweethome.model.dto.ChatDTO;
import com.gitdatsanvich.sweethome.model.dto.ChatMessage;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author TangChen
 * @date 2023/3/16 16:25
 */
@Slf4j
@RestController
@RequestMapping("/ChatGPT")
public class ChatGPTController {
    @Value("${chat-gpt.key}")
    private String CHAT_GPT_KEY;
    @Value("${chat-gpt.url.models}")
    private String MODELS_URL;

    @Value("${chat-gpt.url.talk}")
    private String TALK_URL;

    @GetMapping("/models")
    public R<List<String>> models() {
        String returnString = HttpUtil.sendChatGPT(HttpSendWay.GET, CHAT_GPT_KEY, MODELS_URL, null);
        List<String> modelList = JsonPath.read(returnString, "$.data[*].id");
        return R.ok(modelList);
    }

    @PostMapping("/talk")
    public R<ChatDTO> talk(@RequestBody ChatDTO chatDTO) throws BizException {
        String res = null;
        try {
            if (chatDTO == null) {
                throw BizException.CHAT_GPT_EXCEPTION.newInstance("参数为空");
            }
            res = HttpUtil.sendChatGPT(HttpSendWay.POST, CHAT_GPT_KEY, TALK_URL,
                    this.toTalkMessage(chatDTO.getMessage()));
            String returnString = JsonPath.read(res, "$.choices[0].message.content");
            chatDTO.setMessage(returnString);
        } catch (BizException e) {
            DingDingAlert.pushAlert("ChatGpt错误", Arrays.toString(e.getStackTrace()));
            return R.failed("报错啦 啥问题找ChatGPT去!");
        }
        return R.ok(chatDTO);
    }

    private String toTalkMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(new ChatMessage.Message(message));
        return JSONUtil.toJsonStr(chatMessage);
    }
}
