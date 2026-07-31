package com.creatoros.service;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Development delivery: prints the code to the server log.
 *
 * <p>
 * Deliberately loud and easy to grep, because this is how you retrieve the code
 * during local testing. Replace with an email-backed sender before any real
 * deployment.
 */
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
