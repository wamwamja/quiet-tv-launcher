package com.gathin.tvhome;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Shared clear-water material: transparent centre and local, fading reflections. */
final class ClearWaterDrawable extends Drawable {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path outline=new Path();
    private final float unit,radius;
    private final int idle;
    private boolean focused;
    ClearWaterDrawable(float unit,float radius,int idle){this.unit=unit;this.radius=radius*unit;this.idle=idle;}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] states){boolean next=false;for(int state:states)if(state==android.R.attr.state_focused)next=true;if(next==focused)return false;focused=next;invalidateSelf();return true;}
    @Override public void draw(Canvas canvas){
        RectF box=new RectF(getBounds());
        if(!focused){paint.setShader(null);paint.setColor(idle);canvas.drawRoundRect(box,radius,radius,paint);return;}
        drawWater(canvas,box,radius,unit,paint,outline);
    }
    static void drawWater(Canvas canvas,RectF box,float radius,float unit,Paint paint,Path outline){
        drawWater(canvas,box,radius,unit,paint,outline,false);
    }
    /** A TV footer joins the picture along a straight edge; only its bottom corners are rounded. */
    static void drawFooter(Canvas canvas,RectF box,float radius,float unit,Paint paint,Path outline){
        drawWater(canvas,box,radius,unit,paint,outline,true);
    }
    private static void drawWater(Canvas canvas,RectF box,float radius,float unit,Paint paint,Path outline,boolean footer){
        outline.reset();
        if(footer)outline.addRoundRect(box,new float[]{0,0,0,0,radius,radius,radius,radius},Path.Direction.CW);
        else outline.addRoundRect(box,radius,radius,Path.Direction.CW);
        canvas.save();canvas.clipPath(outline);
        paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);
        paint.setShader(new LinearGradient(0,box.top,0,box.bottom,new int[]{0x13c3d7ea,0x06b6cde0,0x14050e17,0x1abddaef},new float[]{0,.42f,.75f,1},Shader.TileMode.CLAMP));
        canvas.drawRect(box,paint);
        glow(canvas,box,box.left+box.width()*.22f,box.top,box.width()*.52f,box.height()*.55f,0x33dff0ff,paint);
        glow(canvas,box,box.left+box.width()*.82f,box.bottom,box.width()*.48f,box.height()*.48f,0x26a4cee7,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(unit*.85f);
        paint.setShader(new LinearGradient(box.left,0,box.right,0,new int[]{0x00effaff,0x94effaff,0x40effaff,0x00effaff},new float[]{0,.22f,.48f,.73f},Shader.TileMode.CLAMP));
        RectF inner=new RectF(box);inner.inset(1.2f*unit,1.1f*unit);
        // The shared seam is not an exposed glass edge: omit the upper curved reflection too.
        if(!footer){canvas.save();canvas.clipRect(box.left,box.top,box.right,box.top+Math.min(radius,box.height()*.28f));canvas.drawRoundRect(inner,radius,radius,paint);canvas.restore();}
        paint.setShader(new LinearGradient(box.left,0,box.right,0,new int[]{0x00d1edff,0x00d1edff,0x59d1edff,0x00d1edff},new float[]{0,.25f,.78f,1},Shader.TileMode.CLAMP));
        canvas.save();canvas.clipRect(box.left,box.bottom-Math.min(radius,box.height()*.28f),box.right,box.bottom);canvas.drawRoundRect(inner,radius,radius,paint);canvas.restore();
        paint.setShader(null);paint.setStyle(Paint.Style.FILL);canvas.restore();
    }
    private static void glow(Canvas c,RectF box,float x,float y,float rx,float ry,int color,Paint p){
        RadialGradient shader=new RadialGradient(0,0,1,color,Color.TRANSPARENT,Shader.TileMode.CLAMP);Matrix matrix=new Matrix();matrix.setScale(Math.max(rx,1),Math.max(ry,1));matrix.postTranslate(x,y);shader.setLocalMatrix(matrix);p.setShader(shader);c.drawRect(box,p);
    }
    @Override public void setAlpha(int alpha){}
    @Override public void setColorFilter(ColorFilter filter){}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
