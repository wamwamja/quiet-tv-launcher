package com.gathin.tvhome;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

/** Same filled OK key and separated labels as the approved web .key-help/.dialog-footer. */
final class DesignHelp extends LinearLayout {
    private String currentText;private final float scale,textSize;private final int textColor;
    DesignHelp(Context c,float scale,float textSize,int color){super(c);this.scale=scale;this.textSize=textSize;this.textColor=color;setGravity(Gravity.CENTER_VERTICAL);setOrientation(HORIZONTAL);setFocusable(false);}
    private int px(float n){return Math.round(n*scale);}
    void setText(String text){
        if(text.equals(currentText))return;currentText=text;
        removeAllViews();TextView ok=new TextView(getContext());ok.setText("OK");ok.setGravity(Gravity.CENTER);ok.setIncludeFontPadding(false);ok.setTextSize(0,px(7.488f));ok.setTextColor(0xffdbe3ec);ok.setTypeface(null,Typeface.BOLD);
        GradientDrawable bg=new GradientDrawable();bg.setShape(GradientDrawable.OVAL);bg.setColor(0xff303b49);ok.setBackground(bg);addView(ok,new LayoutParams(px(20.16f),px(20.16f)));
        String[] chunks=text.split(" · ");
        for(int i=0;i<chunks.length;i++){
            if(i>0){View divider=new View(getContext());divider.setBackgroundColor(0xff59616c);LayoutParams dl=new LayoutParams(Math.max(1,px(.6f)),px(7.68f));dl.leftMargin=px(9.12f);addView(divider,dl);}
            TextView label=new TextView(getContext());label.setText(chunks[i]);label.setTextSize(0,px(textSize));label.setTextColor(textColor);label.setIncludeFontPadding(false);label.setSingleLine(true);label.setGravity(Gravity.CENTER_VERTICAL);
            LayoutParams lp=new LayoutParams(-2,-1);lp.leftMargin=px(7.2f);addView(label,lp);
        }
    }
}
