package com.creatoros.serviceimpl;

import org.springframework.stereotype.Service;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import com.creatoros.service.OtpSender;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoggingOtpSender implements OtpSender {

    @Override
    public void send(Creator creator, String code, OtpPurpose purpose) {
        log.info("""

                ==================== CreatorOS OTP ====================
                Purpose : {}
                Email   : {}
                Code    : {}
                =======================================================""", purpose, creator.getEmail(), code);
    }
}
