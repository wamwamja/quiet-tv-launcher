package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.tv.TvContract;
import android.media.tv.TvView;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Local S55 customization: one live HDMI session, resized in place, released off Home. */
final class HdmiPreviewPanel extends FrameLayout {
    private static final String INPUT = "com.mediatek.tvinput/.hdmi.HDMIInputService/HW7";
    private final TvView video;
    private final TextView title, hint, status;
    private boolean active, tuned, full;
    private int px, py, pw, ph;
    private final Runnable tuneLater = this::tune;
    HdmiPreviewPanel(Context c) {
        super(c);
        setFocusable(true); setFocusableInTouchMode(true); setClickable(true);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setContentDescription("看电视，HDMI 3，按确定切换 HDMI 3 信号源");
        title = label("看电视", 38, Color.WHITE, true);
        hint = label("机顶盒 · HDMI 3", 18, 0xffc3d1df, false);
        status = label("正在连接信号…", 16, 0xffb5c5d6, false);
        video = new TvView(c); video.setFocusable(false);
        addView(title); addView(hint); addView(status); addView(video);
        video.setCallback(new TvView.TvInputCallback() {
            @Override public void onVideoAvailable(String id) { report("小窗静音 · 按确定看电视"); }
            @Override public void onVideoUnavailable(String id,int why) { report("等待机顶盒画面 · 请确认已开机"); Log.i("S55HomePreview","unavailable="+why); }
            @Override public void onConnectionFailed(String id) { tuned=false; report("暂时无法连接 · 返回首页可重试"); }
            @Override public void onDisconnected(String id) { tuned=false; report("信号已断开 · 请检查机顶盒"); }
            @Override public void onContentBlocked(String id, android.media.tv.TvContentRating r) { report("此节目受电视内容限制"); }
        });
        setOnFocusChangeListener((v,focused)->paint());
        setOnClickListener(v->setFull(!full));
        paint();
    }
    private TextView label(String text,int size,int color,boolean bold) {
        TextView v=new TextView(getContext());v.setText(text);v.setTextSize(size);v.setTextColor(color);
        v.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));v.setIncludeFontPadding(false);
        v.setFocusable(false); return v;
    }
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private void report(String text){status.setText(text);Log.i("S55HomePreview",text);}
    private void paint(){GradientDrawable bg=new GradientDrawable();bg.setColor(full?Color.BLACK:0xff142231);bg.setCornerRadius(full?0:dp(18));bg.setStroke(full?0:dp(isFocused()?3:1),isFocused()?0xfff3f7ff:0xff34495c);setBackground(bg);}
    void place(int x,int y,int w,int h) {
        px=x;py=y;pw=w;ph=h;
        if(full)return;
        boolean show=h>=dp(110)&&w>0;
        if(!show){if(getVisibility()!=GONE){setVisibility(GONE);stopVideo();}return;}
        if(getVisibility()!=VISIBLE)setVisibility(VISIBLE);
        applyBounds();if(active&&!tuned)post(tuneLater);
    }
    private void applyBounds(){
        FrameLayout.LayoutParams lp=full?new FrameLayout.LayoutParams(-1,-1):new FrameLayout.LayoutParams(pw,ph);
        if(!full){lp.leftMargin=px;lp.topMargin=py;}
        android.view.ViewGroup.LayoutParams oldRaw=getLayoutParams();
        FrameLayout.LayoutParams old=oldRaw instanceof FrameLayout.LayoutParams?(FrameLayout.LayoutParams)oldRaw:null;
        if(old==null||old.width!=lp.width||old.height!=lp.height||old.leftMargin!=lp.leftMargin||old.topMargin!=lp.topMargin)setLayoutParams(lp);
        paint();
    }
    @Override protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);layoutChildren(w,h);}
    private void layoutChildren(int w,int h){
        title.setVisibility(full?GONE:VISIBLE);hint.setVisibility(full?GONE:VISIBLE);status.setVisibility(full?GONE:VISIBLE);
        int vh=full?h:Math.max(1,h-dp(20)); int vw=full?w:Math.min(Math.round(vh*16f/9f),w*3/5);
        if(!full)vh=Math.round(vw*9f/16f);
        FrameLayout.LayoutParams vp=new FrameLayout.LayoutParams(vw,vh,full?Gravity.CENTER:Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        if(!full)vp.rightMargin=dp(10);video.setLayoutParams(vp);
        if(!full){int textWidth=Math.max(dp(100),w-vw-dp(55));
            put(title,dp(28),Math.max(dp(15),h/2-dp(58)),textWidth,dp(50));
            put(hint,dp(30),h/2+dp(3),textWidth,dp(28));
            put(status,dp(30),h/2+dp(40),textWidth,dp(44));}
    }
    private void put(View v,int x,int y,int w,int h){FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(w,h);p.leftMargin=x;p.topMargin=y;v.setLayoutParams(p);}
    boolean isFull(){return full;}
    void setFull(boolean value) {
        if (!value) return;
        pausePreview();
        try {
            android.content.Intent intent = new android.content.Intent("com.xiaomi.mitv.tvplayer.EXTSRC_PLAY");
            intent.setComponent(new android.content.ComponentName("com.xiaomi.mitv.tvplayer", "com.xiaomi.mitv.tvplayer.ExternalSourceActivity"));
            intent.putExtra("input", 25);
            getContext().startActivity(intent);
            Log.i("S55HomePreview", "opened native HDMI3 source");
        } catch (RuntimeException e) {
            report("无法打开信号源 · 请使用顶部输入源");
            Log.e("S55HomePreview", "native HDMI3", e);
            resumePreview();
        }
    }
    void resumePreview(){active=true;removeCallbacks(tuneLater);postDelayed(tuneLater,350);}
    void pausePreview(){active=false;removeCallbacks(tuneLater);if(full){full=false;applyBounds();}stopVideo();}
    private void stopVideo(){if(tuned){try{video.reset();}catch(RuntimeException e){Log.w("S55HomePreview","reset",e);}tuned=false;}}
    private void tune(){if(!active||tuned||getVisibility()!=VISIBLE||!isAttachedToWindow())return;
        try{tuned=true;video.setStreamVolume(full?1f:0f);video.tune(INPUT,TvContract.buildChannelUriForPassthroughInput(INPUT));Log.i("S55HomePreview","tune HDMI3");}
        catch(RuntimeException e){tuned=false;report("无法显示预览 · 请使用输入源");Log.e("S55HomePreview","tune",e);}
    }
    @Override protected void onDetachedFromWindow(){active=false;removeCallbacks(tuneLater);stopVideo();super.onDetachedFromWindow();}
}
