package com.minjeok4go.petplace.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentUtil {

    private static final String MERCHANT_UID_PREFIX = "HOTEL_";

    public static String generateMerchantUid(Long reservationId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return MERCHANT_UID_PREFIX + reservationId + "_" + timestamp;
    }

    public static boolean isValidMerchantUid(String merchantUid) {
        return merchantUid != null &&
                merchantUid.startsWith(MERCHANT_UID_PREFIX) &&
                merchantUid.length() > MERCHANT_UID_PREFIX.length();
    }
}
