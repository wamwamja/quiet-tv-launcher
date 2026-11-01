package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.tv.TvInputManager;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Local, remote-first home. The original implementation is retained as LegacyMainActivity. */
public class MainActivity extends Activity {
    static final String ACTION_LAUNCHER_SETTINGS="com.gathin.tvhome.action.LAUNCHER_SETTINGS";
    static final String ACTION_APP_LIBRARY="com.gathin.tvhome.action.APP_LIBRARY";
    static final String ACTION_VIEW_INPUTS="com.android.tv.action.VIEW_INPUTS";
    static int COLUMNS=4;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<App> apps=new ArrayList<>();
    private final List<String> pinned=new ArrayList<>();
    private final List<View> tileViews=new ArrayList<>();
    private final List<Hold> holds=new ArrayList<>();
    private FrameLayout root;
    private ModernPreview preview;
    private SourceCatalog sources;
    private TextView clock,pageLabel;
    private DesignHelp helper;
    private View wifi,settings,libraryButton,back;
    private boolean library,resumed;
    private int page;
    private float unit=1;
    private int width,height;
    private String returnPackage;
    private Dialog modal;
    private final Map<String,ImageView> sourceRows=new HashMap<>();
    private android.content.SharedPreferences prefs;
    private final Runnable ticker=new Runnable(){public void run(){if(clock!=null)clock.setText(new SimpleDateFormat("HH:mm",Locale.CHINA).format(new Date()));handler.postDelayed(this,15000);}};
    private final TvInputManager.TvInputCallback inputEvents=new TvInputManager.TvInputCallback(){
        @Override public void onInputStateChanged(String id,int state){if(preview!=null)preview.inputChanged(id);refreshSourceIndicators();}
        @Override public void onInputAdded(String id){if(preview!=null)preview.inputChanged(id);refreshSourceIndicators();}
        @Override public void onInputRemoved(String id){if(preview!=null)preview.inputChanged(id);refreshSourceIndicators();}
        @Override public void onInputUpdated(String id){if(preview!=null)preview.inputChanged(id);refreshSourceIndicators();}
    };
    private final BroadcastReceiver packages=new BroadcastReceiver(){public void onReceive(Context c,Intent i){loadApps();if(modal==null)render(library);}};
    @Override public void onCreate(Bundle b){super.onCreate(b);DesignAssets.initialize(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        prefs=getSharedPreferences("modern_home",0);sources=new SourceCatalog(this);
        if(sources.manager!=null)sources.manager.registerCallback(inputEvents,handler);
        IntentFilter filter=new IntentFilter(Intent.ACTION_PACKAGE_ADDED);filter.addAction(Intent.ACTION_PACKAGE_REMOVED);filter.addAction(Intent.ACTION_PACKAGE_CHANGED);filter.addDataScheme("package");
        if(Build.VERSION.SDK_INT>=33)registerReceiver(packages,filter,Context.RECEIVER_EXPORTED);else registerReceiver(packages,filter);
        loadApps();root=new FrameLayout(this);root.setBackgroundColor(DesignSpec.BACKGROUND);setContentView(root);
        root.addOnLayoutChangeListener((v,l,t,r,bt,ol,ot,or,ob)->{if(width!=r-l||height!=bt-t){width=r-l;height=bt-t;unit=Math.min(width/960f,height/540f);root.post(()->render(library));}});
        handler.post(ticker);
    }
    @Override protected void onResume(){super.onResume();resumed=true;if(preview!=null&&!library)preview.resume();}
    @Override protected void onPause(){resumed=false;cancelHolds();if(preview!=null)preview.pause();super.onPause();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(sources.manager!=null)sources.manager.unregisterCallback(inputEvents);unregisterReceiver(packages);super.onDestroy();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);dismissModal();
        // HOME reuses the laid-out card and SurfaceView; rebuilding the whole root flashes the page.
        if(library||preview==null)render(false);
        else {preview.requestFocus();if(resumed)preview.resume();}
        root.post(()->{
        if(ACTION_APP_LIBRARY.equals(intent.getAction()))render(true);
        else if(ACTION_VIEW_INPUTS.equals(intent.getAction()))showSources();
        else if(ACTION_LAUNCHER_SETTINGS.equals(intent.getAction()))showSettings();
    });}
    private int u(float x){return Math.round(x*unit);}
    private GradientDrawable bg(int color,boolean focused){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(u(DesignSpec.CORNER));return d;}
    private TextView label(String text,float size,int color){TextView t=new TextView(this);t.setText(text);t.setTextSize(0,u(size));t.setTextColor(color);t.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));t.setGravity(Gravity.CENTER_VERTICAL);t.setIncludeFontPadding(false);return t;}
    private void place(View v,float x,float y,float w,float h){FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(u(w),u(h));p.leftMargin=u(x)+(width-u(960))/2;p.topMargin=u(y)+(height-u(540))/2;root.addView(v,p);}
    private TextView button(String text,Runnable click,Runnable hold){TextView t=label(text,18.24f,DesignSpec.TEXT);t.setGravity(Gravity.CENTER);t.setFocusable(true);t.setId(View.generateViewId());t.setPadding(u(10.56f),0,u(10.56f),0);t.setBackground(bg(0xff252e39,false));t.setOnFocusChangeListener((v,f)->{v.setBackground(bg(f?DesignSpec.FOCUS:0xff252e39,f));if(f&&helper!=null)helper.setText(hold==null?"选择":"选择 · 长按管理");});bind(t,click,hold);return t;}
    private void render(boolean showLibrary){if(width==0)return;cancelHolds();holds.clear();if(preview!=null)preview.pause();root.removeAllViews();tileViews.clear();library=showLibrary;preview=null;
        if(library)renderLibrary();else renderHome();
        clock.setText(new SimpleDateFormat("HH:mm",Locale.CHINA).format(new Date()));
    }
    private void renderHome(){
        clock=label("",44.64f,DesignSpec.TEXT);clock.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));clock.setLetterSpacing(-.043f);place(clock,38.4f,23.73f,112.3f,52.97f);
        place(label(new SimpleDateFormat("M月d日  EEEE",Locale.CHINA).format(new Date()),14.592f,0xffaab3c1),169.87f,39.93f,250,20.54f);
        wifi=button("网络",this::showNetwork,null);icon((TextView)wifi,"wifi",21.12f,7.68f,DesignSpec.TEXT);headerStyle(wifi,"打开网络");place(wifi,739.24f,30.03f,86.39f,40.34f);
        settings=button("设置",this::showSettings,this::openNativeSettings);icon((TextView)settings,"settings",21.12f,7.68f,DesignSpec.TEXT);headerStyle(settings,"打开设置 · 长按原生设置");place(settings,835.22f,30.03f,86.39f,40.34f);
        preview=new ModernPreview(this,sources);preview.setId(View.generateViewId());preview.scale(unit);place(preview,DesignSpec.MARGIN,DesignSpec.HOME_Y,DesignSpec.PREVIEW_WIDTH,DesignSpec.HOME_HEIGHT);
        bind(preview,this::watch,this::showSources);
        preview.setOnFocusChangeListener((v,f)->{preview.focusStyle(f);if(f&&helper!=null)helper.setText("看电视 · 长按切换信号源");});
        List<App> home=new ArrayList<>();for(String pkg:pinned){App a=find(pkg);if(a!=null)home.add(a);if(home.size()==4)break;}
        for(int i=0;i<4;i++){App a=i<home.size()?home.get(i):null;
            View card=a==null?button("添加应用",()->render(true),null):tile(a,true,()->launch(a),()->manage(a));
            tileViews.add(card);place(card,DesignSpec.APP_X+(i%2)*(DesignSpec.APP_WIDTH+DesignSpec.APP_GAP),DesignSpec.HOME_Y+(i/2)*(DesignSpec.APP_HEIGHT+DesignSpec.APP_GAP),DesignSpec.APP_WIDTH,DesignSpec.APP_HEIGHT);
        }
        helper=new DesignHelp(this,unit,12,DesignSpec.MUTED);helper.setText("看电视 · 长按切换信号源");place(helper,38.4f,491.05f,640,20.16f);
        libraryButton=button("全部应用",()->{page=0;render(true);},null);((TextView)libraryButton).setTextSize(0,u(15.36f));icon((TextView)libraryButton,"grid-2x2",17.28f,7.68f,DesignSpec.TEXT);place(libraryButton,808.4f,482.65f,113.2f,36.96f);
        preview.setNextFocusRightId(tileViews.get(0).getId());preview.setNextFocusUpId(wifi.getId());preview.setNextFocusDownId(libraryButton.getId());
        wifi.setNextFocusDownId(preview.getId());wifi.setNextFocusRightId(settings.getId());settings.setNextFocusLeftId(wifi.getId());settings.setNextFocusDownId(tileViews.get(1).getId());
        for(int i=0;i<4;i++){View card=tileViews.get(i);card.setNextFocusLeftId(i%2==0?preview.getId():tileViews.get(i-1).getId());card.setNextFocusRightId(i%2==0?tileViews.get(i+1).getId():card.getId());card.setNextFocusUpId(i<2?(i==0?wifi:settings).getId():tileViews.get(i-2).getId());card.setNextFocusDownId(i<2?tileViews.get(i+2).getId():libraryButton.getId());}
        libraryButton.setNextFocusUpId(tileViews.get(3).getId());libraryButton.setNextFocusLeftId(preview.getId());
        preview.requestFocus();if(resumed)preview.resume();
    }
    private void renderLibrary(){
        back=button("",()->{render(false);libraryButton.requestFocus();},null);back.setPadding(0,0,0,0);((TextView)back).setCompoundDrawables(DesignAssets.icon("arrow-left",u(22.08f),DesignSpec.TEXT),null,null,null);back.setPadding(u(3.84f),0,0,0);headerStyle(back,"返回首页");place(back,38.4f,35.33f,29.76f,29.76f);
        place(label("全部应用",29.76f,DesignSpec.TEXT),96.96f,29.5f,430,41.4f);
        clock=label("",24,DesignSpec.TEXT);clock.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);place(clock,790,33.3f,131.6f,33.8f);
        place(label("选择应用，按确定打开",12.96f,0xffa0acbb),38.4f,91.8f,650,18);
        int pages=Math.max(1,(apps.size()+9)/10);page=Math.min(page,pages-1);
        for(int i=page*10;i<Math.min(apps.size(),page*10+10);i++){App a=apps.get(i);int cell=i-page*10;View v=tile(a,false,()->launch(a),()->manage(a));tileViews.add(v);place(v,38.4f+(cell%5)*(DesignSpec.LIBRARY_WIDTH+DesignSpec.LIBRARY_GAP),DesignSpec.LIBRARY_Y+(cell/5)*(DesignSpec.LIBRARY_HEIGHT+DesignSpec.LIBRARY_GAP),DesignSpec.LIBRARY_WIDTH,DesignSpec.LIBRARY_HEIGHT);}
        if(apps.isEmpty())place(label("暂无可打开的应用",20,0xffc3ccd8),38.4f,180,700,70);
        helper=new DesignHelp(this,unit,12,DesignSpec.MUTED);helper.setText("打开 · 长按管理");place(helper,38.4f,491.05f,620,20.16f);
        pageLabel=label((page+1)+" / "+pages,11.52f,0xffaab6c5);pageLabel.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);place(pageLabel,790,491.05f,131.6f,20.16f);
        for(int i=0;i<tileViews.size();i++){View v=tileViews.get(i);v.setNextFocusLeftId(i%5>0?tileViews.get(i-1).getId():v.getId());v.setNextFocusRightId(i%5<4&&i+1<tileViews.size()?tileViews.get(i+1).getId():v.getId());v.setNextFocusUpId(i>=5?tileViews.get(i-5).getId():back.getId());v.setNextFocusDownId(i+5<tileViews.size()?tileViews.get(i+5).getId():v.getId());}
        back.setNextFocusDownId(tileViews.isEmpty()?back.getId():tileViews.get(0).getId());
        int focus=0;if(returnPackage!=null)for(int i=0;i<tileViews.size();i++)if(apps.get(page*10+i).pkg.equals(returnPackage))focus=i;
        if(!tileViews.isEmpty())tileViews.get(focus).requestFocus();else back.requestFocus();
    }
    private View tile(App app,boolean home,Runnable click,Runnable hold){
        FrameLayout card=new FrameLayout(this);card.setFocusable(true);card.setId(View.generateViewId());card.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        int brandColor=DesignAssets.brandColor(app.pkg);if(DesignAssets.brand(app.pkg)==null&&!DesignAssets.installedBrand(app.pkg))brandColor=utilityColor(utilityIcon(app.pkg));int base=home?brandColor:DesignSpec.CARD;card.setBackground(bg(base,false));
        float cardWidth=home?DesignSpec.APP_WIDTH:DesignSpec.LIBRARY_WIDTH;
        float boxSize=home?73.92f:62.4f,top=home?23.21f:33.4f;
        FrameLayout iconBox=new FrameLayout(this);
        if(!home){GradientDrawable ib=new GradientDrawable();ib.setColor(brandColor);ib.setCornerRadius(u(14.88f));iconBox.setBackground(ib);}
        FrameLayout.LayoutParams boxParams=new FrameLayout.LayoutParams(u(boxSize),u(boxSize));boxParams.leftMargin=u((cardWidth-boxSize)/2);boxParams.topMargin=u(top);card.addView(iconBox,boxParams);
        String asset=DesignAssets.brand(app.pkg);if(asset==null&&!DesignAssets.installedBrand(app.pkg))asset=utilityIcon(app.pkg);
        float iconSize=home?("bilibili".equals(asset)?67.2f:73.92f):(DesignAssets.brand(app.pkg)!=null||DesignAssets.installedBrand(app.pkg)?43.2f:38.4f);
        ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setImageDrawable(asset==null?app.icon:DesignAssets.icon(asset,u(iconSize),0xffffffff));
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(u(iconSize),u(iconSize),Gravity.CENTER);iconBox.addView(image,ip);
        TextView name=label(app.label,home?16.32f:13.44f,DesignSpec.TEXT);name.setGravity(Gravity.CENTER);name.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(u(cardWidth-21.12f),u(home?22.7f:18.7f));np.leftMargin=u(10.56f);np.topMargin=u(top+boxSize+(home?11.52f:10.56f));card.addView(name,np);
        card.setContentDescription(app.label);card.setOnFocusChangeListener((v,f)->{v.setBackground(bg(f?DesignSpec.selectedBrand(base):base,false));if(f&&helper!=null)helper.setText("打开 · 长按管理");});bind(card,click,hold);return card;
    }
    private String utilityIcon(String pkg){
        String name=pkg.toLowerCase(Locale.ROOT);
        if(name.equals("com.xiaomi.mitv.calendar"))return "calendar-days";
        if(name.equals("com.mitv.alarmcenter"))return "timer";
        if(name.equals("com.xiaomi.mitv.shop")||name.equals("com.mitv.shoplugin"))return "shopping-bag";
        if(name.equals("com.xiaomi.mitv.mediaexplorer"))return "folder";
        if(name.equals("com.xiaomi.mitv.smartshare"))return "cast";
        if(name.contains("bluetooth"))return "bluetooth";
        if(name.contains("settings"))return "settings";
        if(name.contains("file"))return "folder";
        if(name.contains("miracast")||name.contains("airplay")||name.contains("cast"))return "cast";
        if(name.contains("gallery")||name.contains("player"))return "play";
        if(name.contains("manager")||name.contains("store"))return "grid-2x2";
        return null;
    }
    private int utilityColor(String icon){if(icon==null)return 0xff3f5568;switch(icon){case "play":return 0xff346b9f;case "folder":return 0xffb18a3d;case "cast":return 0xff39848f;case "grid-2x2":case "timer":return 0xff7566a2;case "settings":return 0xff617387;case "bluetooth":case "calendar-days":return 0xff3a699c;case "shopping-bag":return 0xffb18a3d;default:return 0xff3f5568;}}
    private void headerStyle(View v,String hint){v.setBackground(bg(0,false));v.setOnFocusChangeListener((view,f)->{view.setBackground(bg(f?DesignSpec.FOCUS:0,false));if(f&&helper!=null)helper.setText(hint);});}
    private void icon(TextView v,String kind,float size,float gap,int color){v.setCompoundDrawables(DesignAssets.icon(kind,u(size),color),null,null,null);v.setCompoundDrawablePadding(u(gap));}
    private void loadApps(){
        apps.clear();PackageManager pm=getPackageManager();Set<String> seen=new HashSet<>();Set<String> excluded=new HashSet<>(Arrays.asList(getResources().getStringArray(R.array.library_excluded_apps)));excluded.add(getPackageName());excluded.add("lab.s55.preview");excluded.add("com.mitv.tvhome");
        Map<String,String> labels=new HashMap<>();labels.put("com.ktcp.video","腾讯视频");labels.put("com.gitvdemo.video","爱奇艺");labels.put("com.hunantv.license","芒果 TV");labels.put("com.xiaodianshi.tv.yst","哔哩哔哩");labels.put("com.cibn.tv","优酷");labels.put("com.xiaomi.mitv.settings","电视设置");
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo ri:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)){
            if(ri.activityInfo==null||!ri.activityInfo.exported||!ri.activityInfo.enabled)continue;String pkg=ri.activityInfo.packageName;
            if(excluded.contains(pkg)||pkg.toLowerCase(Locale.ROOT).contains("factory")||!seen.add(pkg))continue;
            apps.add(new App(pkg,labels.getOrDefault(pkg,String.valueOf(ri.loadLabel(pm))),new ComponentName(pkg,ri.activityInfo.name),ri.loadIcon(pm)));
        }
        pinned.clear();String raw=prefs.getString("pinned_v2",String.join(",",getResources().getStringArray(R.array.dock_apps)));for(String p:raw.split(","))if(!p.isEmpty()&&!pinned.contains(p))pinned.add(p);
        List<String> order=Arrays.asList(prefs.getString("library_order_v2","").split(","));
        java.text.Collator collator=java.text.Collator.getInstance(Locale.CHINA);
        apps.sort((a,b)->{int ai=order.indexOf(a.pkg),bi=order.indexOf(b.pkg);if(ai<0)ai=9999;if(bi<0)bi=9999;return ai!=bi?Integer.compare(ai,bi):collator.compare(a.label,b.label);});
        if(!prefs.contains("library_order_v2")){apps.sort(Comparator.comparingInt(a->{int i=pinned.indexOf(a.pkg);return i<0?999:i;}));}
    }
    private App find(String pkg){for(App a:apps)if(a.pkg.equals(pkg))return a;return null;}
    private void launch(App a){returnPackage=a.pkg;open(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(a.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
    private void open(Intent i){try{startActivity(i);}catch(RuntimeException e){toast("无法打开，请检查应用或系统支持");}}
    private void openNativeSettings(){Intent i=SystemSettings.vendorIntent(this);open(i==null?SystemSettings.intent(this,Settings.ACTION_SETTINGS):i);}
    private void watch(){SourceCatalog.Source s=sources.selected();if(s==null){showSources();return;}preview.pause();try{startActivity(sources.playback(s));}catch(RuntimeException e){toast("此电视暂不支持直接播放，请从系统信号源打开");preview.resume();}}
    private void showSettings(){menu("设置",Arrays.asList("电视设置","原生设置","应用管理"),i->{if(i==0)open(SystemSettings.intent(this,Settings.ACTION_SETTINGS));if(i==1)openNativeSettings();if(i==2)open(SystemSettings.intent(this,Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));},0);}
    private void showSources(){List<SourceCatalog.Source> list=sources.list();SourceCatalog.Source current=sources.selected();List<String> rows=new ArrayList<>();int selected=0;
        for(int i=0;i<list.size();i++){SourceCatalog.Source s=list.get(i);boolean chosen=current!=null&&current.id.equals(s.id);rows.add(s.label);if(chosen)selected=i;}
        if(rows.isEmpty()){toast("没有发现可用信号源");return;}
        menu("选择信号源",rows,i->{sources.select(list.get(i));preview.refresh();preview.requestFocus();},selected,list);
    }
    private void manage(App app){returnPackage=app.pkg;List<String> entries=new ArrayList<>();boolean onHome=pinned.contains(app.pkg);entries.add(onHome?"从首页移除":"添加到首页");entries.add("调整应用排序");if(onHome)entries.add("调整首页位置");
        menu(app.label,entries,i->{if(i==0){if(onHome){pinned.remove(app.pkg);savePins();render(library);}else pin(app);}else if(i==1)reorder(app,false);else reorder(app,true);},0);
    }
    private void pin(App a){if(pinned.size()<4){pinned.add(a.pkg);savePins();render(library);return;}List<String> rows=new ArrayList<>();for(String p:pinned){App old=find(p);rows.add("替换 "+(old==null?p:old.label));}menu("首页已满，选择替换",rows,i->{pinned.set(i,a.pkg);savePins();render(library);},0);}
    private void savePins(){prefs.edit().putString("pinned_v2",String.join(",",pinned)).apply();}
    private void reorder(App a,boolean home){List<String> rows=new ArrayList<>(),ids=new ArrayList<>();if(home)ids.addAll(pinned);else for(App x:apps)ids.add(x.pkg);
        for(int i=0;i<ids.size();i++){App x=find(ids.get(i));rows.add("第 "+(i+1)+" 位 · "+(x==null?ids.get(i):x.label));}
        menu(home?"移动到首页位置":"移动到应用位置",rows,i->{ids.remove(a.pkg);ids.add(Math.min(i,ids.size()),a.pkg);if(home){pinned.clear();pinned.addAll(ids);savePins();}else prefs.edit().putString("library_order_v2",String.join(",",ids)).apply();loadApps();if(!home)page=Math.max(0,ids.indexOf(a.pkg)/10);render(library);},Math.max(0,ids.indexOf(a.pkg)));
    }
    private void refreshSourceIndicators(){
        for(Map.Entry<String,ImageView> entry:sourceRows.entrySet()){
            boolean connected=false;try{connected=sources.manager!=null&&sources.manager.getInputState(entry.getKey())==TvInputManager.INPUT_STATE_CONNECTED;}catch(RuntimeException ignored){}
            entry.getValue().setVisibility(connected?View.VISIBLE:View.GONE);
            entry.getValue().setContentDescription(connected?"信号源已连接":"未报告连接");
        }
    }
    private void showNetwork(){menu("网络",Arrays.asList("无线网络","网络设置"),i->open(SystemSettings.intent(this,i==0?Settings.ACTION_WIFI_SETTINGS:Settings.ACTION_WIRELESS_SETTINGS)),0);}
    private boolean networkConnected(){android.net.ConnectivityManager cm=(android.net.ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);return cm!=null&&cm.getActiveNetwork()!=null;}
    private boolean wirelessConnected(){android.net.ConnectivityManager cm=(android.net.ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);if(cm==null)return false;android.net.NetworkCapabilities nc=cm.getNetworkCapabilities(cm.getActiveNetwork());return nc!=null&&nc.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI);}
    private void menu(String title,List<String> rows,java.util.function.IntConsumer select,int initial){menu(title,rows,select,initial,null);}
    private void menu(String title,List<String> rows,java.util.function.IntConsumer select,int initial,List<SourceCatalog.Source> inputList){
        cancelHolds();dismissModal();sourceRows.clear();Dialog d=new Dialog(this);modal=d;d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(u(23.04f),u(23.04f),u(23.04f),u(23.04f));GradientDrawable panelBg=bg(DesignSpec.PANEL,false);panelBg.setCornerRadius(u(15.36f));panel.setBackground(panelBg);
        String headingIcon=title.equals("设置")?"settings":title.equals("选择信号源")?"monitor":title.equals("网络")?"wifi":"grid-2x2";
        TextView heading=label(title,21.6f,DesignSpec.TEXT);heading.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));icon(heading,headingIcon,23.04f,8.64f,0xffcfdae6);panel.addView(heading,new LinearLayout.LayoutParams(-1,u(30.27f)));
        String subtitle=inputList!=null?"绿色图标表示已连接；勾号表示当前选择。":title.equals("网络")?(networkConnected()?"网络已连接":"网络未连接"):"常用功能，简单直达。";
        TextView sub=label(subtitle,10.368f,0xff8fa0b4);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,u(14.05f));sp.topMargin=u(6.72f);sp.bottomMargin=u(16.32f);panel.addView(sub,sp);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);LinearLayout choices=new LinearLayout(this);choices.setOrientation(LinearLayout.VERTICAL);scroll.addView(choices);
        SourceCatalog.Source selectedSource=sources.selected();List<View> buttons=new ArrayList<>();
        for(int i=0;i<rows.size();i++){
            final int index=i;LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(u(11.52f),0,u(11.52f),0);row.setFocusable(true);row.setId(View.generateViewId());row.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);row.setBackground(bg(DesignSpec.ROW,false));row.setOnFocusChangeListener((v,f)->v.setBackground(bg(f?DesignSpec.FOCUS:DesignSpec.ROW,false)));bind(row,()->{d.dismiss();select.accept(index);},null);
            String rowIcon=inputList!=null?(rows.get(i).startsWith("AV")?"plug":"monitor"):title.equals("设置")?(i==0?"monitor":i==1?"sliders-horizontal":"grid-2x2"):title.equals("网络")?(i==0?"wifi":"sliders-horizontal"):rows.get(i).contains("位置")||rows.get(i).contains("排序")?"arrow-up-down":"pin";
            ImageView left=new ImageView(this);left.setImageDrawable(DesignAssets.icon(rowIcon,u(18.24f),0xffb8c7d9));row.addView(left,new LinearLayout.LayoutParams(u(18.24f),u(18.24f)));
            TextView text=label(rows.get(i),16.32f,DesignSpec.TEXT);text.setSingleLine(true);text.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-1,1);tp.leftMargin=u(9.6f);row.addView(text,tp);
            if(inputList!=null){
                SourceCatalog.Source source=inputList.get(i);
                if(selectedSource!=null&&source.id.equals(selectedSource.id)){ImageView check=new ImageView(this);check.setImageDrawable(DesignAssets.icon("check",u(15.84f),0xffdbe6f0));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(u(15.84f),u(15.84f));cp.leftMargin=u(9.6f);row.addView(check,cp);check.setContentDescription("当前选择");}
                ImageView signal=new ImageView(this);signal.setImageDrawable(DesignAssets.icon("monitor-check",u(17.28f),DesignSpec.CONNECTED));LinearLayout.LayoutParams signalParams=new LinearLayout.LayoutParams(u(17.28f),u(17.28f));signalParams.leftMargin=u(9.6f);row.addView(signal,signalParams);sourceRows.put(source.id,signal);
            }else if(title.equals("网络")&&i==0){TextView state=label(wirelessConnected()?"已连接":"未连接",10.08f,wirelessConnected()?DesignSpec.CONNECTED:0xff8fa0b4);row.addView(state,new LinearLayout.LayoutParams(-2,-1));}
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,u(40.58f));if(i<rows.size()-1)lp.bottomMargin=u(6.24f);choices.addView(row,lp);buttons.add(row);
        }
        if(inputList!=null)refreshSourceIndicators();
        int scrollHeight=u(Math.min(274,rows.size()*40.58f+Math.max(0,rows.size()-1)*6.24f));panel.addView(scroll,new LinearLayout.LayoutParams(-1,scrollHeight));
        DesignHelp tip=new DesignHelp(this,unit,10.08f,0xff93a3b5);tip.setText("确认选择 · 返回取消");LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,u(20.16f));hp.topMargin=u(15.36f);panel.addView(tip,hp);
        d.setOnKeyListener((dialog,key,event)->handleMenuKey(event));
        d.setContentView(panel);d.setOnDismissListener(x->{if(modal==d){modal=null;sourceRows.clear();}cancelHolds();if(Build.VERSION.SDK_INT>=31)root.setRenderEffect(null);root.post(()->{if(helper!=null)helper.setText(library?"打开 · 长按管理":preview!=null&&preview.hasFocus()?"看电视 · 长按切换信号源":"选择");});});
        Window w=d.getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setDimAmount(.66f);}
        if(Build.VERSION.SDK_INT>=31)root.setRenderEffect(RenderEffect.createBlurEffect(u(5.76f),u(5.76f),Shader.TileMode.CLAMP));
        d.show();w.setLayout(u(422.4f),-2);buttons.get(Math.min(initial,buttons.size()-1)).requestFocus();
    }
    private void dismissModal(){if(modal!=null){Dialog d=modal;modal=null;d.dismiss();sourceRows.clear();}if(root!=null&&Build.VERSION.SDK_INT>=31)root.setRenderEffect(null);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void bind(View v,Runnable click,Runnable longClick){Hold h=new Hold(v,click,longClick);holds.add(h);v.setOnKeyListener(h);v.setOnClickListener(x->click.run());if(longClick!=null)v.setOnLongClickListener(x->{longClick.run();return true;});}
    private void cancelHolds(){for(Hold h:holds)h.cancel();}
    private final class Hold implements View.OnKeyListener {
        final View view;final Runnable click,longClick;final PressGesture gesture=new PressGesture();
        final Runnable fire=this::fireLong;
        private void fireLong(){if(view.hasFocus()&&longClick!=null&&gesture.longPress())longClick.run();}
        Hold(View v,Runnable c,Runnable l){view=v;click=c;longClick=l;}
        void cancel(){handler.removeCallbacks(fire);gesture.cancel();}
        public boolean onKey(View v,int key,KeyEvent event){if(key!=KeyEvent.KEYCODE_DPAD_CENTER&&key!=KeyEvent.KEYCODE_ENTER&&key!=KeyEvent.KEYCODE_NUMPAD_ENTER)return false;
            if(event.getAction()==KeyEvent.ACTION_DOWN){if(event.getRepeatCount()==0){gesture.down(event.getEventTime());if(longClick!=null)handler.postDelayed(fire,600);}else if(event.isLongPress())fire.run();return true;}
            if(event.getAction()==KeyEvent.ACTION_UP){handler.removeCallbacks(fire);PressGesture.Action action=gesture.up(event.getEventTime(),event.isCanceled(),longClick!=null);if(action==PressGesture.Action.LONG)longClick.run();else if(action==PressGesture.Action.SHORT)click.run();return true;}return true;
        }
    }
    /** Menu is a global launcher shortcut, including while a launcher dialog owns focus. */
    private boolean handleMenuKey(KeyEvent event){
        if(event.getKeyCode()!=KeyEvent.KEYCODE_MENU)return false;
        if(RemoteKeys.menuReleased(event.getAction(),event.getFlags())){
            cancelHolds();dismissModal();
            try{
                ComponentName started=startService(new Intent("com.xiaomi.mitv.smartpanel.action.SHOW_PANEL")
                    .setComponent(new ComponentName("com.xiaomi.mitv.smartpanel","com.xiaomi.mitv.smartpanel.SmartPanelService")));
                if(started==null)showSettings();
            }catch(RuntimeException unavailable){showSettings();}
        }
        return true;
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e){
        if(handleMenuKey(e))return true;
        if(modal!=null)return super.dispatchKeyEvent(e);
        if(library&&(e.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN||e.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP)){
            int index=tileViews.indexOf(getCurrentFocus());boolean next=e.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN&&index>=5&&page*10+10<apps.size();boolean prev=e.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP&&index>=0&&index<5&&page>0;
            if(next||prev){if(e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()==0){int col=index%5;page+=next?1:-1;returnPackage=null;render(true);int focus=Math.min(tileViews.size()-1,next?col:col+5);if(focus>=0)tileViews.get(focus).requestFocus();}return true;}
        }
        return super.dispatchKeyEvent(e);
    }
    @Override public void onBackPressed(){if(modal!=null){dismissModal();return;}if(library){render(false);libraryButton.requestFocus();}else if(preview!=null)preview.requestFocus();}
    private static final class App{final String pkg,label;final ComponentName component;final Drawable icon;App(String p,String l,ComponentName c,Drawable i){pkg=p;label=l;component=c;icon=i;}}
}
