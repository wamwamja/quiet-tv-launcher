package com.gathin.tvhome;

import android.content.*;
import android.media.tv.*;
import android.os.Build;
import java.util.*;

/** Enumerates the current TV; vendor numbers are scoped to the verified S55 adapter only. */
final class SourceCatalog {
    static final class Source {
        final String id, label; final Integer vendor;
        Source(String id,String label,Integer vendor){this.id=id;this.label=label;this.vendor=vendor;}
    }
    private final Context context;
    final TvInputManager manager;
    final String deviceKey;
    SourceCatalog(Context c) {
        context=c; manager=(TvInputManager)c.getSystemService(Context.TV_INPUT_SERVICE);
        deviceKey="source:"+Build.MANUFACTURER+":"+Build.MODEL+":"+Build.DEVICE;
    }
    static boolean verifiedXiaomi(){return "MiTV-MFTR0".equals(Build.MODEL)&&"freeguy".equals(Build.DEVICE);}
    static Integer xiaomiRoute(String id) {
        if(!id.startsWith("com.mediatek.tvinput/")) return null;
        switch(id.substring(id.lastIndexOf('/')+1)) {
            case "HW5": return 23; case "HW6":return 24; case "HW7":return 25;
            case "HW2":return 2; case "HW8":return -1; default:return null;
        }
    }
    List<Source> list() {
        List<Source> result=new ArrayList<>(); if(manager==null)return result;
        for(TvInputInfo info:manager.getTvInputList()) {
            if(!info.isPassthroughInput()||info.isHidden(context))continue;
            Integer route=verifiedXiaomi()?xiaomiRoute(info.getId()):null;
            if(route!=null&&route<0)continue;
            String label=String.valueOf(info.loadLabel(context));
            if(route!=null) label=route==2?"AV":"HDMI "+(route-22);
            String alias=context.getSharedPreferences("modern_home",0).getString("alias:"+deviceKey+":"+info.getId(),"");
            if(alias.isEmpty()){CharSequence custom=info.loadCustomLabel(context);if(custom!=null&&!custom.toString().equals(label))alias=custom.toString();}
            if(!alias.isEmpty())label+=" · "+alias;
            result.add(new Source(info.getId(),label,route));
        }
        result.sort(Comparator.comparing((Source a)->!a.label.startsWith("HDMI")).thenComparing(a->a.label)); return result;
    }
    Source selected() {
        SharedPreferences p=context.getSharedPreferences("modern_home",0);
        String id=p.getString(deviceKey,"");
        // One-time migration of the user's previously verified HDMI3; never applies on another TV.
        if(id.isEmpty()&&!p.contains(deviceKey)&&verifiedXiaomi())id="com.mediatek.tvinput/.hdmi.HDMIInputService/HW7";
        for(Source s:list())if(s.id.equals(id))return s;
        return null;
    }
    void select(Source s){context.getSharedPreferences("modern_home",0).edit().putString(deviceKey,s.id).apply();}
    Intent playback(Source s) {
        if(s.vendor!=null)return new Intent("com.xiaomi.mitv.tvplayer.EXTSRC_PLAY")
            .setComponent(new ComponentName("com.xiaomi.mitv.tvplayer","com.xiaomi.mitv.tvplayer.ExternalSourceActivity")).putExtra("input",s.vendor);
        return new Intent(Intent.ACTION_VIEW,TvContract.buildChannelUriForPassthroughInput(s.id));
    }
}
