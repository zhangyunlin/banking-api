package com.tsb.banking.auth.sms;

public interface SmsSender {

    void send(String phoneNumber, String message);

}
