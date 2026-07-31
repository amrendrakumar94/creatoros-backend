package com.creatoros.service;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;

public interface OtpSender {

    void send(Creator creator, String code, OtpPurpose purpose);
}
