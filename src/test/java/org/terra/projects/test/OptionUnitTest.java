package org.terra.projects.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    
    @Test
    public void testGetValue() {
    	Option option = new Option("b", null);
    	
    	option.setArgs(Option.UNLIMITED_VALUES);
    	
    	assertEquals("default", option.getValue("default"));
    	assertEquals(null, option.getValue());
    	
    	option.addValueForProcessing("test");
        
        assertEquals("test", option.getValue());
        assertEquals("test", option.getValue(0));
        assertEquals("test", option.getValue("default"));
    }
    
}
