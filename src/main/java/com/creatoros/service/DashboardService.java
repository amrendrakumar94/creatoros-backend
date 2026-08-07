package com.creatoros.service;

import com.creatoros.dto.dashboard.DashboardDto;

public interface DashboardService {

    DashboardDto summary(Long creatorId, String financialYear);
}
