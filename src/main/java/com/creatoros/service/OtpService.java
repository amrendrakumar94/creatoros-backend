package com.creatoros.service;

import com.creatoros.entity.Creator;
import com.creatoros.entity.OtpPurpose;

public interface OtpService {

    void issue(Creator creator, OtpPurpose purpose);

    void verify(Creator creator, OtpPurpose purpose, String code);
}
