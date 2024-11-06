package com.eazybytes.accounts.service.clients;

import com.eazybytes.accounts.dto.LoansDto;
import org.springframework.http.ResponseEntity;
import com.eazybytes.accounts.service.client.LoansFeignClient;
import org.springframework.stereotype.Component;


@Component
public class LoansFallback implements LoansFeignClient {
    @Override
    public ResponseEntity<LoansDto> fetchLoanDetails(String correlationId, String mobileNumber) {
        return null;
    }
}
