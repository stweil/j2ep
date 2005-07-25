package net.sf.j2ep.test;

import net.sf.j2ep.factories.ResponseHandlerFactory;
import junit.framework.TestCase;

public class AllowHeaderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testprocessAllowHeader() {
        String allow = "OPTIONS,PROPFIND,OP,PUT";
        String correct = "OPTIONS,PUT,";
        String returned = ResponseHandlerFactory.processAllowHeader(allow);
        assertEquals("Checking factory implementation for allow header", correct, returned);
    }

}
