package org.terra.projects.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.terra.projects.terracli.Option;

public class OptionUnitTest {
    
    private static class TestOption extends Option {
        private static final long serialVersionUID = 1L;

        public TestOption(String opt, boolean hasArg, String description) throws IllegalArgumentException
        {
            super(opt, hasArg, description);
        }
    }
    
    @Test
    public void testClear() {
        TestOption test = new TestOption("a", true, "");
        assertEquals(0, test.getValuesList().size());
    }
    
    @Test
    public void testHasArgs() {
    	Option option = new Option("a", null);
    	
    	option.setArgs(0);
    	assertFalse(option.hasArgs());
    	
    	option.setArgs(1);
    	assertFalse(option.hasArgs());
    	
    	option.setArgs(2);
    	assertTrue(option.hasArgs());
    	
    	option.setArgs(Option.UNLIMITED_VALUES);
    	assertTrue(option.hasArgs());
    	
    	option.setArgs(Option.UNINITIALIZED);
    	assertFalse(option.hasArgs());
    }
    
}
