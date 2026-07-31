package com.creatoros.service;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;

/**
 * Delivery channel for one-time codes.
 *
 * <p>
 * The only implementation today is {@link LoggingOtpSender}, which writes the
 * code to the server log so the app is usable with no SMTP setup. Swapping in
 * an email or SMS implementation requires no changes above this interface.
 */
public interface OtpSender {

    void send(Creator creator, String code, OtpPurpose purpose);
}
