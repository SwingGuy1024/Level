package com.neptunedreams.util;

public class ProductTestNativeImpl {
    public String getPurchaseTestId() {
        return "android.test.purchased";
    }

    public String getUnavailableTestId() {
        return "android.test.item_unavailable";
    }

    public String getCanceledTestId() {
        return "android.test.canceled";
    }

    public String getRefundedTestId() {
        return "android.test.refunded";
    }

    public boolean isSupported() {
        return true;
    }

}
