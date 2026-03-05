package com.tjoeun.boxmon.global.toss.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 토스페이먼츠 결제 객체 (Payment) DTO
 * @see <a href="https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4">Toss Payments API Docs</a>
 */
@Data
public class TossPayment {
    private String version;
    private String paymentKey;
    private String type; // NORMAL, BILLING, BRANDPAY
    private String orderId;
    private String orderName;
    private String mId;
    private String currency;
    @NotNull
    private String method; // 카드, 가상계좌, 간편결제 등
    private Long totalAmount;
    private Long balanceAmount;
    private String status; // READY, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED
    private String requestedAt;
    private String approvedAt;
    private Boolean useEscrow;
    private String lastTransactionKey;
    private Long suppliedAmount;
    private Long vat;
    private Boolean cultureExpense;
    private Long taxFreeAmount;
    private Integer taxExemptionAmount;
    private List<Cancel> cancels;
    private Card card;
    private VirtualAccount virtualAccount;
    private MobilePhone mobilePhone;
    private GiftCertificate giftCertificate;
    private Transfer transfer;
    private Receipt receipt;
    private Checkout checkout;
    private EasyPay easyPay;
    private Failure failure;
    private CashReceipt cashReceipt;
    private List<CashReceipt> cashReceipts;
    private Discount discount;
    private Boolean isPartialCancelable;
    private String country;

    @Data
    public static class Card {
        private Long amount;
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private Integer installmentPlanMonths;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType; // 신용, 체크, 기프트, 미확인
        private String ownerType; // 개인, 법인, 미확인
        private String acquireStatus; // READY, SUCCESS, COMPLETED, CANCEL_OR_REFUND, FAILED
        private Boolean isInterestFree;
        private String interestFreeConfig;
    }

    @Data
    public static class VirtualAccount {
        private String accountType; // 일반, 고정
        private String accountNumber;
        private String bankCode;
        private String customerName;
        private String dueDate;
        private String refundStatus; // NONE, PENDING, FAILED, PARTIAL_FAILED, COMPLETED
        private Boolean expired;
        private String settlementStatus; // INCOMPLETE, COMPLETED
    }

    @Data
    public static class Cancel {
        private Long cancelAmount;
        private String cancelReason;
        private Long taxFreeAmount;
        private Integer taxExemptionAmount;
        private Long refundableAmount;
        private Long easyPayDiscountAmount;
        private String canceledAt;
        private String transactionKey;
        private String receiptKey;
    }

    @Data
    public static class EasyPay {
        private String provider;
        private Long amount;
        private Long discountAmount;
    }

    @Data
    public static class Failure {
        private String code;
        private String message;
    }

    @Data
    public static class Receipt {
        private String url;
    }

    @Data
    public static class Checkout {
        private String url;
    }

    @Data
    public static class CashReceipt {
        private String type; // 소득공제, 지출증빙
        private String receiptKey;
        private String issueNumber;
        private String receiptUrl;
        private Long amount;
        private Long taxFreeAmount;
    }

    @Data
    public static class Discount {
        private Long amount;
    }

    @Data
    public static class Transfer {
        private String bankCode;
        private String settlementStatus;
    }

    @Data
    public static class MobilePhone {
        private String customerMobilePhone;
        private String settlementStatus;
    }

    @Data
    public static class GiftCertificate {
        private String approveNo;
        private String settlementStatus;
    }
}
