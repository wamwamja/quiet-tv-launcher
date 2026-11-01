package com.gathin.tvhome;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Vector controls independent of installed icon fonts. */
final class UiIcon extends Drawable {
 private final String kind;private final int color;private final Paint p=new Paint(3);
 UiIcon(String kind,int size,int color){this.kind=kind;this.color=color;setBounds(0,0,size,size);}
 @Override public void draw(Canvas c){c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/24f,getBounds().height()/24f);p.setColor(color);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
 switch(kind){
 case "wifi":p.setStrokeWidth(2.7f);for(int i=0;i<3;i++){float r=4+i*5;c.drawArc(12-r,20-r,12+r,20+r,225,90,false,p);}p.setStyle(Paint.Style.FILL);c.drawCircle(12,19,1.5f,p);break;
 case "settings":Path gear=new Path();for(int i=0;i<48;i++){double angle=i*Math.PI/24;float r=(i%6==0||i%6==5)?8:10;float x=12+(float)Math.cos(angle)*r,y=12+(float)Math.sin(angle)*r;if(i==0)gear.moveTo(x,y);else gear.lineTo(x,y);}gear.close();gear.addCircle(12,12,3.6f,Path.Direction.CCW);gear.setFillType(Path.FillType.EVEN_ODD);p.setStyle(Paint.Style.FILL);c.drawPath(gear,p);break;
 case "grid":p.setStyle(Paint.Style.FILL);for(int y=0;y<2;y++)for(int x=0;x<2;x++)c.drawRoundRect(3+x*11,3+y*11,10+x*11,10+y*11,1.5f,1.5f,p);break;
 case "mute":Path s=new Path();s.moveTo(3,9);s.lineTo(7,9);s.lineTo(12,5);s.lineTo(12,19);s.lineTo(7,15);s.lineTo(3,15);s.close();p.setStyle(Paint.Style.FILL);c.drawPath(s,p);p.setStyle(Paint.Style.STROKE);c.drawLine(17,9,22,15,p);c.drawLine(22,9,17,15,p);break;
 case "ok":c.drawCircle(12,12,10,p);p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(8);p.setTextAlign(Paint.Align.CENTER);c.drawText("OK",12,15,p);break;
 case "back":c.drawLine(15,5,8,12,p);c.drawLine(8,12,15,19,p);break;
 default:c.drawRoundRect(2,4,22,17,2,2,p);c.drawLine(8,21,16,21,p);c.drawLine(12,17,12,21,p);if(kind.equals("signal")){p.setStyle(Paint.Style.FILL);c.drawCircle(12,10,3,p);}
 }c.restore();}
 @Override public void setAlpha(int a){p.setAlpha(a);}
 @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
