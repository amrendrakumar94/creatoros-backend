package com.creatoros.service;

import com.creatoros.entity.Creator;
import com.creatoros.enums.OtpPurpose;

public interface OtpSender {

    void send(Creator creator, String code, OtpPurpose purpose);
}
