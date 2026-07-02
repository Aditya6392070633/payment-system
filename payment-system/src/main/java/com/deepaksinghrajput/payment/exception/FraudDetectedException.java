package com.deepaksinghrajput.payment.exception;

public class FraudDetectedException extends RuntimeException {
    private final int score;

    public FraudDetectedException(String message, int score) {
        super(message);
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
