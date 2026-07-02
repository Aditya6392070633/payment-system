package com.deepaksinghrajput.payment.controller;

import com.deepaksinghrajput.payment.dto.RefundRequest;
import com.deepaksinghrajput.payment.dto.RefundResponse;
import com.deepaksinghrajput.payment.entity.Refund;
import com.deepaksinghrajput.payment.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody RefundRequest request) {
        RefundResponse response = refundService.processRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/by-payment/{paymentId}")
    public ResponseEntity<List<Refund>> getRefundsForPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(refundService.getRefundsForPayment(paymentId));
    }
}
