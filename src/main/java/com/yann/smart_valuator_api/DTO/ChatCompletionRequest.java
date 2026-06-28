package com.yann.smart_valuator_api.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Request payload for the OpenAI-compatible chat completions API used by Hugging Face.
 * Field names match the API spec exactly (snake_case) for direct JSON serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionRequest {

    /** ID of the model to use (e.g. "meta-llama/Llama-3.3-70B-Instruct"). */
    public String model;

    /** List of messages forming the conversation context. */
    public List<Message> messages;

    /** Sampling temperature — lower values produce more deterministic output. */
    public double temperature;

    /** Maximum number of tokens to generate in the response. */
    public int max_tokens;

    /**
     * A single message in the chat conversation.
     * Role is typically "user" or "assistant".
     */
    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

