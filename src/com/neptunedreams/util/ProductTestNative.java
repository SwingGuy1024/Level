package com.neptunedreams.util;

import com.codename1.system.NativeInterface;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 7/27/16
 * <p>Time: 3:41 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface ProductTestNative extends NativeInterface {
	String getPurchaseTestId();
	String getCanceledTestId();
	String getRefundedTestId();
	String getUnavailableTestId();
}
