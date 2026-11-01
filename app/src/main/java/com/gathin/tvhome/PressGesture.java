package com.gathin.tvhome;

/** Pure gesture state: one physical OK press can produce at most one action. */
final class PressGesture {
    enum Action { NONE, SHORT, LONG }
    private boolean down, consumed;
    private long started;
    void down(long time){down=true;consumed=false;started=time;}
    boolean longPress(){if(!down||consumed)return false;consumed=true;return true;}
    Action up(long time,boolean canceled,boolean hasLongAction){
        if(!down)return Action.NONE;
        down=false;
        if(consumed||canceled)return Action.NONE;
        consumed=true;
        return hasLongAction&&time-started>=600?Action.LONG:Action.SHORT;
    }
    void cancel(){down=false;consumed=true;}
}
