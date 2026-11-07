package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class FocusNavigationTest {
    @Test public void homeEdgesDoNotJumpToUnrelatedControls(){
        assertEquals(FocusNavigation.TV,FocusNavigation.home(FocusNavigation.TV,FocusNavigation.LEFT));
        assertEquals(FocusNavigation.NETWORK,FocusNavigation.home(FocusNavigation.NETWORK,FocusNavigation.UP));
        assertEquals(FocusNavigation.SETTINGS,FocusNavigation.home(FocusNavigation.SETTINGS,FocusNavigation.RIGHT));
        assertEquals(1,FocusNavigation.home(1,FocusNavigation.RIGHT));
        assertEquals(3,FocusNavigation.home(3,FocusNavigation.RIGHT));
    }
    @Test public void bottomControlsLeadToTransitionGate(){
        for(int current:new int[]{FocusNavigation.TV,2,3})assertEquals(FocusNavigation.GATE,FocusNavigation.home(current,FocusNavigation.DOWN));
        assertEquals(2,FocusNavigation.home(0,FocusNavigation.DOWN));
        assertEquals(3,FocusNavigation.home(1,FocusNavigation.DOWN));
    }
    @Test public void partialLastRowKeepsAReachableFocus(){
        assertEquals(11,FocusNavigation.grid(9,12,5,FocusNavigation.DOWN));
        assertEquals(11,FocusNavigation.grid(11,12,5,FocusNavigation.RIGHT));
        assertEquals(11,FocusNavigation.grid(11,12,5,FocusNavigation.DOWN));
        assertEquals(6,FocusNavigation.grid(11,12,5,FocusNavigation.UP));
    }
    @Test public void everyGridSizeStaysInsideItsBoundsAndNeverWrapsHorizontally(){
        for(int count=1;count<=101;count++)for(int i=0;i<count;i++)for(int direction=0;direction<4;direction++){
            int next=FocusNavigation.grid(i,count,5,direction);
            if(next==FocusNavigation.GATE){assertEquals(FocusNavigation.UP,direction);assertTrue(i<5);}
            else{assertTrue(next>=0&&next<count);if(direction==FocusNavigation.LEFT||direction==FocusNavigation.RIGHT)assertEquals(i/5,next/5);}
        }
    }
    @Test public void scrollingMovesOneRowAndRestoresEarlierRows(){
        assertEquals(0,FocusNavigation.firstVisibleRow(9,0,2));
        assertEquals(1,FocusNavigation.firstVisibleRow(10,0,2));
        assertEquals(2,FocusNavigation.firstVisibleRow(19,1,2));
        assertEquals(1,FocusNavigation.firstVisibleRow(5,2,2));
        assertEquals(0,FocusNavigation.firstVisibleRow(0,1,2));
    }
    @Test public void emptyAndInvalidGridInputsAreStable(){
        assertEquals(-1,FocusNavigation.grid(-1,0,5,FocusNavigation.DOWN));
        assertEquals(0,FocusNavigation.grid(0,0,5,FocusNavigation.DOWN));
        assertEquals(0,FocusNavigation.grid(0,1,0,FocusNavigation.DOWN));
    }
}
