package com.afarms.transaction.service;

import com.afarms.transaction.model.dto.DashboardReportDTO;

public interface TransactionService {
    DashboardReportDTO getDashboardData(int range, String authHeader);
}