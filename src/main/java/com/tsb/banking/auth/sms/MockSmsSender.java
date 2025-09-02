package com.tsb.banking.auth.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Mock SMS sender that logs the OTP instead of sending an actual SMS.
 * In a real application, integrate with an SMS gateway like Twilio or Nexmo.
 */
@Component
public class MockSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(MockSmsSender.class);

    @Override
    public void send(String phoneNumber, String message) {

        // Log the OTP, mocking the sending SMS process
        log.info("Mock SMS to {}: {}", mask(phoneNumber), message);
    }

    private String mask(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
