package com.tradex.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class VerificationCodeStore {

    private final ConcurrentHashMap<String, String> codes = new ConcurrentHashMap<>();

    public void store(String email, String code) {
        codes.put(email, code);
    }

    /** Retrieves and removes the code (one-time use). */
    public String retrieve(String email) {
        return codes.remove(email);
    }

    public boolean has(String email) {
        return codes.containsKey(email);
    }
}
