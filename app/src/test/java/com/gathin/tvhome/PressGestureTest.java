package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class PressGestureTest {
    @Test public void shortPressOpensOnlyOnce(){PressGesture g=new PressGesture();g.down(10);assertEquals(PressGesture.Action.SHORT,g.up(150,false,true));assertEquals(PressGesture.Action.NONE,g.up(160,false,true));}
    @Test public void longPressReleaseDoesNotOpenPlayback(){PressGesture g=new PressGesture();g.down(10);assertTrue(g.longPress());assertFalse(g.longPress());assertEquals(PressGesture.Action.NONE,g.up(900,false,true));}
    @Test public void delayedTimerStillProducesOnlyLongAction(){PressGesture g=new PressGesture();g.down(10);assertEquals(PressGesture.Action.LONG,g.up(800,false,true));assertFalse(g.longPress());}
    @Test public void lifecycleCancelCannotActivateNewScreen(){PressGesture g=new PressGesture();g.down(10);g.cancel();assertFalse(g.longPress());assertEquals(PressGesture.Action.NONE,g.up(900,false,true));}
    @Test public void canceledKeyUpDoesNothing(){PressGesture g=new PressGesture();g.down(10);assertEquals(PressGesture.Action.NONE,g.up(100,true,true));}
    @Test public void nextPressWorksAfterLongPress(){PressGesture g=new PressGesture();g.down(10);g.longPress();g.up(800,false,true);g.down(1000);assertEquals(PressGesture.Action.SHORT,g.up(1100,false,true));}
    @Test public void buttonWithoutLongActionStillClicks(){PressGesture g=new PressGesture();g.down(10);assertEquals(PressGesture.Action.SHORT,g.up(900,false,false));}
}
