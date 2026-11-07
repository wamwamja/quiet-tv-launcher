package com.gathin.tvhome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** A transient water reflection. Never translates a hardware TV surface. */
final class BoundaryFeedback {
    private long lastPulse=-300;
    private ValueAnimator animator;
    private View staticView;
    private Drawable staticEdge;
    private Runnable clearStatic;
    static int direction(int key) {
        switch(key){
            case KeyEvent.KEYCODE_DPAD_LEFT:return View.FOCUS_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:return View.FOCUS_RIGHT;
            case KeyEvent.KEYCODE_DPAD_UP:return View.FOCUS_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:return View.FOCUS_DOWN;
            default:return 0;
        }
    }
    void cancel(){if(animator!=null){animator.cancel();animator=null;}if(staticView!=null){staticView.removeCallbacks(clearStatic);staticView.getOverlay().remove(staticEdge);staticView=null;staticEdge=null;clearStatic=null;}}
    void pulse(View view,int key,float unit){
        long now=SystemClock.uptimeMillis();
        if(view==null||!view.hasFocus()||!view.isAttachedToWindow()||direction(key)==0||now-lastPulse<300)return;
        lastPulse=now;cancel();
        boolean television=view instanceof ModernPreview;
        boolean motion=ValueAnimator.areAnimatorsEnabled();
        final DecelerateInterpolator easing=new DecelerateInterpolator();
        WaterEdge edge=new WaterEdge(key,unit,television);
        edge.setBounds(0,0,view.getWidth(),view.getHeight());view.getOverlay().add(edge);
        float x=view.getTranslationX(),y=view.getTranslationY();
        float dx=key==KeyEvent.KEYCODE_DPAD_LEFT?-2.5f:key==KeyEvent.KEYCODE_DPAD_RIGHT?2.5f:0;
        float dy=key==KeyEvent.KEYCODE_DPAD_UP?-2.5f:key==KeyEvent.KEYCODE_DPAD_DOWN?2.5f:0;
        if(!motion){edge.strength=.65f;edge.invalidateSelf();staticView=view;staticEdge=edge;clearStatic=this::cancel;view.postDelayed(clearStatic,180);return;}
        ValueAnimator pulse=ValueAnimator.ofFloat(0,1);animator=pulse;pulse.setDuration(260);pulse.setInterpolator(new android.view.animation.LinearInterpolator());
        pulse.addUpdateListener(a->{
            if(!view.hasFocus()||!view.isAttachedToWindow()){a.cancel();return;}
            float t=(float)a.getAnimatedValue();float envelope=t<.3f?t/.3f:(1-t)/.7f;
            edge.strength=.85f*envelope;edge.invalidateSelf();
            if(!television){float rebound=t<.27f?t/.27f:Math.max(0,(.85f-t)/.58f);rebound=easing.getInterpolation(rebound);view.setTranslationX(x+dx*unit*rebound);view.setTranslationY(y+dy*unit*rebound);}
        });
        pulse.addListener(new AnimatorListenerAdapter(){@Override public void onAnimationEnd(Animator a){view.getOverlay().remove(edge);if(!television){view.setTranslationX(x);view.setTranslationY(y);}if(animator==a)animator=null;}});pulse.start();
    }
    private static final class WaterEdge extends Drawable {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final Path clip=new Path();
        final int key;final float unit;final boolean tv;float strength;
        WaterEdge(int key,float unit,boolean tv){this.key=key;this.unit=unit;this.tv=tv;}
        @Override public void draw(Canvas c){
            RectF b=new RectF(getBounds());float radius=Math.min(14.4f*unit,b.height()/2),depth=tv?3*unit:Math.min(b.width(),b.height())*.28f;
            float x=b.centerX(),y=b.centerY(),ex=x,ey=y;
            if(key==KeyEvent.KEYCODE_DPAD_LEFT){x=b.left;ex=x+depth;}else if(key==KeyEvent.KEYCODE_DPAD_RIGHT){x=b.right;ex=x-depth;}else if(key==KeyEvent.KEYCODE_DPAD_UP){y=b.top;ey=y+depth;}else{y=b.bottom;ey=y-depth;}
            clip.reset();clip.addRoundRect(b,radius,radius,Path.Direction.CW);c.save();c.clipPath(clip);
            paint.setShader(new LinearGradient(x,y,ex,ey,new int[]{Color.argb(Math.round(180*strength),224,248,255),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRect(b,paint);paint.setShader(null);c.restore();
        }
        @Override public void setAlpha(int alpha){}
        @Override public void setColorFilter(ColorFilter filter){}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
