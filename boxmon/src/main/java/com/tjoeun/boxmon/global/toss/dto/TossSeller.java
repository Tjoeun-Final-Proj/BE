package com.tjoeun.boxmon.global.toss.dto;

import lombok.Data;
import java.util.Map;

/**
 * 토스페이먼츠 셀러 객체 (Seller) DTO
 * @see <a href="https://docs.tosspayments.com/reference/additional#seller-%EA%B0%9D%EC%B2%B4">Toss Payments API Docs</a>
 */
@Data
public class TossSeller {
    private String id;
    private String refSellerId;
    private String businessType; // INDIVIDUAL, INDIVIDUAL_BUSINESS, CORPORATE
    private Company company;
    private Individual individual;
    private String status; // APPROVAL_REQUIRED, PARTIALLY_APPROVED, KYC_REQUIRED, APPROVED
    private Account account;
    private Map<String, Object> metadata;

    @Data
    public static class Company {
        private String name;
        private String representativeName;
        private String businessRegistrationNumber;
        private String email;
        private String phone;
    }

    @Data
    public static class Individual {
        private String name;
        private String email;
        private String phone;
    }

    @Data
    public static class Account {
        private String bankCode;
        private String accountNumber;
        private String holderName;
    }
}
