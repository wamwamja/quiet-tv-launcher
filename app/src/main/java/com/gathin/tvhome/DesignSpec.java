package com.gathin.tvhome;

/** Measurements normalized from the approved #screen container to a 960 x 540 design plane. */
final class DesignSpec {
    static final int BACKGROUND=0xff171d25, TEXT=0xfff1f4f7, FOCUS=0xff465466;
    static final int CARD=0xff28323e, PREVIEW=0xff242e39, PANEL=0xff222c39, ROW=0xff2b3543;
    static final int MUTED=0xffaeb9c8, CONNECTED=0xff83d9b4;
    static final float MARGIN=38.4f, CORNER=11.04f;
    static final float HOME_Y=118.8f, HOME_HEIGHT=324f, PREVIEW_WIDTH=491.05f;
    static final float APP_X=549.6f, APP_WIDTH=178.56f, APP_HEIGHT=154.56f, APP_GAP=14.88f;
    static final float LIBRARY_Y=135f, LIBRARY_WIDTH=165.888f, LIBRARY_HEIGHT=157.98f, LIBRARY_GAP=13.44f;
    static int selectedBrand(int color){return android.graphics.Color.rgb(
        Math.round(android.graphics.Color.red(color)*.68f+255*.32f),
        Math.round(android.graphics.Color.green(color)*.68f+255*.32f),
        Math.round(android.graphics.Color.blue(color)*.68f+255*.32f));}
}
