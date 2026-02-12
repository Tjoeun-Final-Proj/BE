package com.tjoeun.boxmon;

import com.tjoeun.boxmon.feature.payment.controller.PaymentController;
import com.tjoeun.boxmon.feature.payment.dto.ConfirmPaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.com.tjoeun.boxmon.feature.payment=DEBUG"
})
public class BoxmonApplicationTests {
    
    @Autowired
    PaymentController paymentController;

    @Test
    void contextLoads() {
    }

    @Test
    void mockPaymentTest(){
        var request = new ConfirmPaymentRequest();
        request.setPaymentKey("tviva20260212150725s4fR9");
        request.setShipmentId(2L);
        paymentController.confirmPayment(request);
    }
}
