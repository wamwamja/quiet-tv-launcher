package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.media.tv.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

/** Muted preview only. System source player owns all full-screen playback and audio. */
final class ModernPreview extends FrameLayout {
    private final TvView video;
    private final TextView status,detail,caption,sourceLabel;
    private final LinearLayout statusPanel;
    private final ImageView statusIcon,mute;
    private final Edge edge;
    private final SourceCatalog sources;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean active,tuned,available;
    private String tuningId;
    private int generation;
    private float unit=1;
    private final Runnable delayedTune=this::tune;
    ModernPreview(Context c,SourceCatalog sources) {
        super(c);this.sources=sources;
        setFocusable(true);setFocusableInTouchMode(true);setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setBackgroundColor(DesignSpec.PREVIEW);setClipChildren(true);setClipToPadding(true);
        video=new TvView(c);video.setFocusable(false);video.setZOrderMediaOverlay(false);addView(video);
        statusPanel=new LinearLayout(c);statusPanel.setOrientation(LinearLayout.VERTICAL);statusPanel.setGravity(Gravity.CENTER);statusPanel.setBackgroundColor(0xff202a37);addView(statusPanel);
        statusIcon=new ImageView(c);statusPanel.addView(statusIcon);
        status=text("正在连接…",18);status.setGravity(Gravity.CENTER);status.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));statusPanel.addView(status);
        detail=text("",11);detail.setGravity(Gravity.CENTER);detail.setTextColor(0xff8d9db1);statusPanel.addView(detail);
        caption=text("电视",16);caption.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));addView(caption);
        sourceLabel=text("请选择信号源",13);sourceLabel.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);sourceLabel.setTextColor(0xffc0cbd8);addView(sourceLabel);
        mute=new ImageView(c);mute.setContentDescription("预览已静音");mute.setScaleType(ImageView.ScaleType.CENTER_INSIDE);android.graphics.drawable.GradientDrawable badge=new android.graphics.drawable.GradientDrawable();badge.setShape(android.graphics.drawable.GradientDrawable.OVAL);badge.setColor(0xb3132027);mute.setBackground(badge);addView(mute);
        edge=new Edge(c);addView(edge);edge.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        connecting();setOnFocusChangeListener((v,f)->focusStyle(f));
    }
    private TextView text(String s,int size){TextView t=new TextView(getContext());t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setIncludeFontPadding(false);return t;}
    void focusStyle(boolean focused){setBackgroundColor(focused?DesignSpec.FOCUS:DesignSpec.PREVIEW);edge.invalidate();}
    void scale(float u){unit=u;caption.setTextSize(0,16.512f*u);sourceLabel.setTextSize(0,12.96f*u);status.setTextSize(0,18.24f*u);detail.setTextSize(0,10.56f*u);detail.setLineSpacing(7.68f*u,1);
        mute.setPadding(Math.round(6.96f*u),Math.round(6.96f*u),Math.round(6.96f*u),Math.round(6.96f*u));mute.setImageDrawable(DesignAssets.icon("volume-x",Math.round(15.84f*u),Color.WHITE));
        caption.setCompoundDrawables(DesignAssets.icon("monitor",Math.round(18.72f*u),DesignSpec.TEXT),null,null,null);caption.setCompoundDrawablePadding(Math.round(7.68f*u));
        statusIcon.setImageDrawable(DesignAssets.icon("monitor-x",Math.round(38.4f*u),0xff64778e));statusIcon.setLayoutParams(new LinearLayout.LayoutParams(Math.round(38.4f*u),Math.round(38.4f*u)));
        LinearLayout.LayoutParams titleParams=new LinearLayout.LayoutParams(-2,-2);titleParams.topMargin=Math.round(12.48f*u);status.setLayoutParams(titleParams);
        LinearLayout.LayoutParams detailParams=new LinearLayout.LayoutParams(-2,-2);detailParams.topMargin=Math.round(7.68f*u);detail.setLayoutParams(detailParams);requestLayout();}
    private void box(View v,int x,int y,int w,int h){LayoutParams p=(LayoutParams)v.getLayoutParams();if(p!=null&&p.width==w&&p.height==h&&p.leftMargin==x&&p.topMargin==y)return;p=new LayoutParams(w,h);p.leftMargin=x;p.topMargin=y;v.setLayoutParams(p);}
    @Override protected void onMeasure(int ws,int hs){int w=MeasureSpec.getSize(ws),h=MeasureSpec.getSize(hs),pad=0,vh=Math.round((w-2*pad)*9f/16f);int footer=h-vh-pad;
        box(video,pad,pad,w-2*pad,vh);box(statusPanel,pad,pad,w-2*pad,vh);
        box(caption,Math.round(15.36f*unit),vh+pad,Math.round(w*.28f),footer);
        caption.setGravity(Gravity.CENTER_VERTICAL);
        box(sourceLabel,Math.round(w*.31f),vh+pad,w-Math.round(w*.31f)-Math.round(15.36f*unit),footer);
        box(mute,w-Math.round(41.76f*unit),Math.round(12*unit),Math.round(29.76f*unit),Math.round(29.76f*unit));
        box(edge,0,0,w,h);super.onMeasure(ws,hs);
    }
    void resume(){if(active)return;active=true;handler.removeCallbacks(delayedTune);handler.postDelayed(delayedTune,300);}
    void pause(){active=false;handler.removeCallbacksAndMessages(null);stop();}
    void refresh(){handler.removeCallbacks(delayedTune);stop();if(active)handler.postDelayed(delayedTune,200);else refreshCaption();}
    private void stop(){connecting();generation++;available=false;tuned=false;tuningId=null;video.setCallback(null);video.reset();}
    private void refreshCaption(){SourceCatalog.Source s=sources.selected();sourceLabel.setText(s==null?"请选择信号源":s.label);setContentDescription("电视，"+sourceLabel.getText()+"，按确定观看，长按切换信号源");}
    /** An opaque, fixed-size cover stays in place until video is ready. No changing loading layout. */
    private void connecting(){statusPanel.setVisibility(VISIBLE);statusIcon.setVisibility(INVISIBLE);status.setVisibility(INVISIBLE);detail.setVisibility(INVISIBLE);mute.setVisibility(INVISIBLE);status.setText("正在连接…");}
    private void show(String message){if(message.equals("正在连接…")){connecting();return;}
        String[] parts=message.split("\\n\\n",2);status.setText(parts[0]);detail.setText(parts.length>1?parts[1]:"");
        statusIcon.setVisibility(VISIBLE);status.setVisibility(VISIBLE);detail.setVisibility(parts.length>1?VISIBLE:INVISIBLE);statusPanel.setVisibility(VISIBLE);mute.setVisibility(INVISIBLE);}
    private void tune(){if(!active||tuned||!isAttachedToWindow())return;
        refreshCaption();SourceCatalog.Source selected=sources.selected();
        if(selected==null){show("请选择信号源\n\n长按 OK 选择");return;}
        final String id=selected.id;final int token=++generation;tuningId=id;available=false;
        video.setCallback(new TvView.TvInputCallback(){
            private boolean current(String input){return active&&token==generation&&id.equals(input);}
            @Override public void onVideoAvailable(String input){if(!current(input))return;available=true;statusPanel.setVisibility(INVISIBLE);mute.setVisibility(VISIBLE);Log.i("ModernPreview","VIDEO_AVAILABLE "+id);}
            @Override public void onVideoUnavailable(String input,int reason){if(!current(input))return;available=false;
                if(reason==TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING||reason==TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING)show("正在连接…");
                else if(reason==TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL||disconnected())showNoSignal();
                else if(reason==TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY)show("当前信号仅有音频\n\n按确定进入系统播放");
                else show("预览暂不可用\n\n按确定尝试系统播放");
                Log.i("ModernPreview","unavailable="+reason+" "+id);
            }
            @Override public void onConnectionFailed(String input){if(current(input)){tuned=false;show("预览暂不可用\n\n按确定尝试系统播放");retry(token);}}
            @Override public void onDisconnected(String input){if(current(input)){tuned=false;show("预览连接已断开\n\n正在重试…");retry(token);}}
            @Override public void onContentBlocked(String input,TvContentRating rating){if(current(input))show("此节目受电视内容限制");}
        });
        show(disconnected()?"无信号\n\n请检查设备电源或连接\n长按 OK 切换信号源":"正在连接…");
        try{tuned=true;video.setStreamVolume(0f);video.tune(id,TvContract.buildChannelUriForPassthroughInput(id));
            handler.postDelayed(()->{if(active&&token==generation&&!available){if(disconnected())showNoSignal();else if(status.getText().toString().equals("正在连接…"))show("预览暂不可用\n\n按确定尝试系统播放");}},7000);
        }catch(RuntimeException e){tuned=false;show("预览暂不可用\n\n按确定尝试系统播放");Log.e("ModernPreview","tune",e);}
    }
    private void retry(int token){handler.postDelayed(()->{if(active&&token==generation){stop();tune();}},4000);}
    private boolean disconnected(){try{return tuningId!=null&&sources.manager.getInputState(tuningId)==TvInputManager.INPUT_STATE_DISCONNECTED;}catch(RuntimeException e){return false;}}
    private void showNoSignal(){show("无信号\n\n请检查设备电源或连接\n长按 OK 切换信号源");}
    void inputChanged(String id){SourceCatalog.Source s=sources.selected();if(s==null||s.id.equals(id)){if(active)refresh();}}
    @Override protected void onDetachedFromWindow(){pause();super.onDetachedFromWindow();}
    /** Opaque corner masks also cover the hardware video plane; no stretching of the signal. */
    private final class Edge extends View {
        final Paint paint=new Paint(3);final Path outside=new Path();
        Edge(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){float inset=0,r=14.4f*unit;RectF bounds=new RectF(inset,inset,getWidth()-inset,getHeight()-inset);
            outside.reset();outside.setFillType(Path.FillType.EVEN_ODD);outside.addRect(0,0,getWidth(),getHeight(),Path.Direction.CW);outside.addRoundRect(bounds,r,r,Path.Direction.CW);
            paint.setStyle(Paint.Style.FILL);paint.setColor(DesignSpec.BACKGROUND);c.drawPath(outside,paint);
            
        }
    }
}
