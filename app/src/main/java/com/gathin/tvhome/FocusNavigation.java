package com.gathin.tvhome;

/** Logical directions, independent of Android geometry and partially filled rows. */
final class FocusNavigation {
    static final int UP=0,DOWN=1,LEFT=2,RIGHT=3;
    static final int TV=-1,NETWORK=-2,SETTINGS=-3,GATE=-4;
    static int home(int current,int direction){
        if(current==TV)return direction==UP?NETWORK:direction==DOWN?GATE:direction==RIGHT?0:TV;
        if(current==NETWORK)return direction==DOWN?TV:direction==RIGHT?SETTINGS:NETWORK;
        if(current==SETTINGS)return direction==DOWN?1:direction==LEFT?NETWORK:SETTINGS;
        if(current<0||current>3)return current;
        switch(direction){
            case UP:return current<2?(current==0?NETWORK:SETTINGS):current-2;
            case DOWN:return current<2?current+2:GATE;
            case LEFT:return current%2==0?TV:current-1;
            case RIGHT:return current%2==0?current+1:current;
            default:return current;
        }
    }
    static int grid(int current,int count,int columns,int direction){
        if(current<0||current>=count||columns<1)return current;
        switch(direction){
            case UP:return current<columns?GATE:current-columns;
            case DOWN:return current+columns<count?current+columns:current/columns<(count-1)/columns?count-1:current;
            case LEFT:return current%columns>0?current-1:current;
            case RIGHT:return current%columns<columns-1&&current+1<count?current+1:current;
            default:return current;
        }
    }
    static int firstVisibleRow(int current,int first,int visibleRows){int row=Math.max(0,current)/5;return row<first?row:row>=first+visibleRows?row-visibleRows+1:first;}
}
