package com.neptunedreams;

import com.neptunedreams.vulcan.app.LevelOfVulcan;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/21/16
 * <p>Time: 11:09 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@NonNls
public final class Assert {
	private Assert() { }
	@Contract("false->fail")
	public static void doAssert(boolean condition) {
		doAssert(condition, "");
	}

	@Contract("false,_->fail")
	public static void doAssert(boolean condition, @NonNls Object message) {
		if (LevelOfVulcan.debug && !condition) {
//		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
