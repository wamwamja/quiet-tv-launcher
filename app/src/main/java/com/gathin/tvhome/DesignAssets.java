package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import com.caverock.androidsvg.SVG;
import java.util.*;

/** Uses the approved web SVG documents directly. No approximated paths or icon-font substitutions. */
final class DesignAssets {
    private static Context context;
    private static final Map<String,Bitmap> bitmaps=new HashMap<>();
    static void initialize(Context c){context=c.getApplicationContext();}
    static Drawable icon(String name,int size,int color){
        if(context==null)throw new IllegalStateException("Design assets not initialized");
        final int pixels=Math.max(1,size*2);String key=name+":"+pixels;
        Bitmap bitmap=bitmaps.get(key);
        if(bitmap==null){try{
            SVG svg=SVG.getFromAsset(context.getAssets(),"approved-design/"+name+".svg");
            svg.setDocumentWidth(pixels);svg.setDocumentHeight(pixels);
            bitmap=Bitmap.createBitmap(pixels,pixels,Bitmap.Config.ARGB_8888);
            svg.renderToCanvas(new Canvas(bitmap));
            bitmaps.put(key,bitmap);
        }catch(Exception e){throw new IllegalStateException("Cannot render approved SVG: "+name,e);}}
        BitmapDrawable d=new BitmapDrawable(context.getResources(),bitmap);d.setFilterBitmap(true);d.setTint(color);d.setBounds(0,0,size,size);return d;
    }
    static String brand(String pkg){
        if(pkg.equals("com.xiaodianshi.tv.yst"))return "bilibili";
        return null;
    }
    // These brands use the installed app drawable; no downloaded logo is redistributed.
    static boolean installedBrand(String pkg){return pkg.equals("com.ktcp.video")||pkg.equals("com.gitvdemo.video")||pkg.equals("com.hunantv.license");}
    static int brandColor(String pkg){
        if(pkg.equals("com.ktcp.video"))return 0xff245da2;
        if(pkg.equals("com.gitvdemo.video"))return 0xff18865b;
        if(pkg.equals("com.hunantv.license"))return 0xffc3622a;
        if(pkg.equals("com.xiaodianshi.tv.yst"))return 0xffb64b76;
        return 0xff3f5568;
    }
}
