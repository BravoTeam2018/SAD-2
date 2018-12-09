package com.cit.notifier.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateId {
    private static final Logger log = LoggerFactory.getLogger(GenerateId.class);

    public static String generateClientId(){
        String generatedString = generateSafeToken();
        if (log.isInfoEnabled()) {
            log.info(String.format("new client ID is: %s", generatedString));
        }
        return generatedString;
    }

    private static String generateSafeToken() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[15];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }
}
