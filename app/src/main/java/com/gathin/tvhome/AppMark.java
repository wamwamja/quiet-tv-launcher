package com.gathin.tvhome;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Consistent vector treatment of the four home brands, without app-store badge backgrounds. */
final class AppMark extends Drawable {
    private final int brand;private final boolean home;private final Paint p=new Paint(3);
    static Drawable forPackage(String pkg,boolean home){int b=pkg.equals("com.ktcp.video")?0:pkg.equals("com.gitvdemo.video")?1:pkg.equals("com.hunantv.license")?2:pkg.equals("com.xiaodianshi.tv.yst")?3:-1;return b<0?null:new AppMark(b,home);}
    private AppMark(int b,boolean h){brand=b;home=h;}
    @Override public int getIntrinsicWidth(){return 100;}
    @Override public int getIntrinsicHeight(){return 100;}
    private void fill(int color){p.setColor(color);p.setStyle(Paint.Style.FILL);p.setPathEffect(null);p.setShader(null);}
    @Override public void draw(Canvas c){c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/100f,getBounds().height()/100f);
        if(!home){int[] colors={0xff237bdd,0xff1cb956,0xfffd761e,0xffed659d};fill(colors[brand]);c.drawRoundRect(0,0,100,100,21,21,p);c.translate(10,10);c.scale(.8f,.8f);}
        if(brand==0){
            triangle(c,new float[]{32,7,90,42,90,55,32,94,22,87,22,15},0xff1cb5ef);
            triangle(c,new float[]{12,18,73,39,78,53,12,83,6,76,6,26},0xffffd331);
            triangle(c,new float[]{31,22,78,45,78,55,31,82,24,76,24,29},0xff69d62f);
            triangle(c,new float[]{39,34,63,48,63,53,39,67,35,64,35,38},Color.WHITE);
        }else if(brand==1){
            fill(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);c.drawRoundRect(3,21,97,78,13,13,p);
            p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));p.setTextSize(34);p.setTextAlign(Paint.Align.CENTER);c.drawText("iQIYI",50,63,p);
        }else if(brand==2){
            fill(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
            Path border=new Path();border.moveTo(25,12);border.cubicTo(12,8,8,18,8,30);border.lineTo(8,79);border.cubicTo(8,91,17,94,30,89);border.lineTo(84,66);border.cubicTo(96,61,97,50,84,44);border.lineTo(39,24);c.drawPath(border,p);
            Path m=new Path();m.moveTo(27,70);m.lineTo(27,32);m.lineTo(45,64);m.lineTo(63,34);m.lineTo(63,69);c.drawPath(m,p);
        }else{
            fill(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            c.drawRoundRect(9,27,91,84,12,12,p);c.drawLine(30,9,43,24,p);c.drawLine(69,8,57,24,p);c.drawLine(30,47,38,51,p);c.drawLine(62,51,70,47,p);
            Path smile=new Path();smile.moveTo(36,65);smile.quadTo(43,75,50,65);smile.quadTo(57,75,64,65);c.drawPath(smile,p);c.drawLine(26,87,24,93,p);c.drawLine(74,87,76,93,p);
        }c.restore();}
    private void triangle(Canvas c,float[] points,int color){fill(color);p.setPathEffect(new CornerPathEffect(6));Path path=new Path();path.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);path.close();c.drawPath(path,p);p.setPathEffect(null);}
    @Override public void setAlpha(int a){p.setAlpha(a);}
    @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
