package com.itextos.beacon.commonlib.unittest;

import com.itextos.beacon.commonlib.pattern.PatternCache;
import com.itextos.beacon.commonlib.pattern.PatternCheckCategory;

public class PatternTest {

	public boolean doSuccessTest() {
		
        return PatternCache.getInstance().isPatternMatch(PatternCheckCategory.HEADER_CHECK, "test", "test");

	}
	
	public boolean doFailTest() {
		
        return !PatternCache.getInstance().isPatternMatch(PatternCheckCategory.HEADER_CHECK, "test", "best");

	}
}
