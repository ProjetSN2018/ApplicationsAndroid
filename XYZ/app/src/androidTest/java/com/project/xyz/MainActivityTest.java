package com.project.xyz;

import junit.framework.TestCase;

import org.junit.Test;

public class MainActivityTest extends TestCase{
    private MainActivity mainActivity;

    @Test
    public void testPow(){
        int result = mainActivity.pow(5);
        assertEquals(25,result);
    }
//    @Test
//    public void testPow2(){
//        int result = mainActivity.pow(5);
//        assertEquals(24,result);
//    }
}
