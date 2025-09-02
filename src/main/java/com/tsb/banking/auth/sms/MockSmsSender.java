package com.tsb.banking.auth.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(MockSmsSender.class);

    @Override
    public void send(String phoneNumber, String message) {

        // Log the OTP, mocking the sending SMS process
        log.info("Mock SMS to {}: {}", mask(phoneNumber), message);
    }

    private String mask(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
