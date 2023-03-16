package com.gitdatsanvich.sweethome.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {
    private String model = "gpt-3.5-turbo";

    private List<Message> messages;

    public ChatMessage(Message message) {
        this.messages = Collections.singletonList(message);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        public Message(String content) {
            this.content = content;
        }

        private String role = "user";

        private String content;
    }
}
