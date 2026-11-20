package org.oppo.drawergroups;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class Entry implements IXposedHookLoadPackage {
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.launcher".equals(p.packageName) || !"com.android.launcher".equals(p.processName)) return;
        try {
            Class<?> data = p.classLoader.loadClass("com.android.launcher3.allapps.cluster.ClusterLayoutData");
            Class<?> item = p.classLoader.loadClass("com.android.launcher3.allapps.BaseAllAppsAdapter$AdapterItem");
            Field apps = field(data, "mApps"), target = field(data, "mAdapterItems");
            Field columns = field(data, "mNumColumn"), section = field(item, "sectionName");
            Method refreshRows = data.getDeclaredMethod("refreshRawMum");
            refreshRows.setAccessible(true);
            Field rowCount = field(data, "mRawCount");
            Field heightField = field(data, "mItemHeight");
            Method itemHeight = data.getDeclaredMethod("getItemHeight");
            Method margin = data.getDeclaredMethod("getLayoutBeyondMargin");
            Class<?> iconClass = p.classLoader.loadClass("com.android.launcher3.allapps.cluster.OplusClusterIconLayout");
            Class<?> layoutClass = p.classLoader.loadClass("com.android.launcher3.allapps.cluster.OplusClusterLayout");
            Field iconField = field(layoutClass, "mIconLayout"), labelField = field(layoutClass, "mTvSection");
            final List<RowPlan.Row>[] active = new List[]{null};
            final String[] currentSection={null};
            final float[] lastHeight={-1f};
            XposedHelpers.findAndHookMethod(data, "refreshOffsetY", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam h) {
                    try { float v=heightField.getFloat(null); if(v>0 && Math.abs(v-lastHeight[0])>0.5f) { lastHeight[0]=v*0.78f; heightField.setFloat(null,lastHeight[0]); } } catch(Throwable ignored) {}
                }
                @Override protected void afterHookedMethod(MethodHookParam h) {
                    try {
                        List<RowPlan.Row> rows=active[0]; if(rows==null || rows.isEmpty()) return;
                        int index=-1; for(int i=0;i<rows.size();i++) if(currentSection[0].equals(rows.get(i).label)) { index=i; break; }
                        if(index<0) return;
                        Object layout=field(data,"mLayout").get(null); if(!(layout instanceof View)) return;
                        float y=((Number)layoutClass.getDeclaredMethod("getSectionMid").invoke(layout)).floatValue();
                        float gap=((View)layout).getResources().getDisplayMetrics().density*12f;
                        float height=heightField.getFloat(null);
                        field(data,"mOffsetY").setFloat(null,y-height*0.5f-index*height-rows.get(index).group*gap);
                    } catch(Throwable e) { XposedBridge.log("OPPO Drawer Groups spacing: "+e); }
                }
            });
            XposedHelpers.findAndHookMethod(iconClass, "layoutSingleRaw", float.class, int.class, int.class, ArrayList.class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam h) {
                    try {
                        List<RowPlan.Row> rows=active[0]; if(rows==null || rows.isEmpty()) return;
                        float height=((Number)itemHeight.invoke(null)).floatValue();
                        float base=((Number)margin.invoke(null)).floatValue();
                        int index=Math.round((((Number)h.args[0]).floatValue()-base)/height);
                        if(index<0 || index>=rows.size()) return;
                        RowPlan.Row r=rows.get(index);
                        float gap=((View)h.thisObject).getResources().getDisplayMetrics().density*12f;
                        h.args[0]=base+index*height+r.group*gap;
                        h.args[1]=r.start; h.args[2]=r.end;
                    } catch(Throwable e) { XposedBridge.log("OPPO Drawer Groups row: "+e); }
                }
            });
            XposedHelpers.findAndHookMethod(data, "getIconLayoutHeight", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam h) {
                    try {
                        List<RowPlan.Row> rows=active[0]; if(rows==null || rows.isEmpty()) return;
                        Object layout=field(data,"mLayout").get(null); if(!(layout instanceof View)) return;
                        float gap=((View)layout).getResources().getDisplayMetrics().density*12f;
                        h.setResult(((Number)h.getResult()).floatValue()+rows.get(rows.size()-1).group*gap);
                    } catch(Throwable e) { XposedBridge.log("OPPO Drawer Groups height: "+e); }
                }
            });
            XposedHelpers.findAndHookMethod(iconClass, "dispatchDraw", Canvas.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam h) {
                    try {
                        List<RowPlan.Row> rows=active[0]; if(rows==null) return;
                        ViewGroup icons=(ViewGroup)h.thisObject;
                        icons.setClipChildren(false);
                        if(icons.getParent() instanceof ViewGroup) ((ViewGroup)icons.getParent()).setClipChildren(false);
                        Canvas canvas=(Canvas)h.args[0]; Paint paint=new Paint(3); paint.setColor(0xffffffff);
                        float density=icons.getResources().getDisplayMetrics().scaledDensity; paint.setTextSize(17*density);
                        for(RowPlan.Row r:rows) {
                            if(r.label==null || r.start>=icons.getChildCount()) continue;
                            View child=icons.getChildAt(r.start); if(child.getVisibility()!=View.VISIBLE) continue;
                            float x=child.getLeft()-paint.measureText(r.label)-6*density;
                            float y=child.getTop()+child.getHeight()*0.35f;
                            canvas.drawText(r.label,x,y,paint);
                        }
                    } catch(Throwable e) { XposedBridge.log("OPPO Drawer Groups labels: "+e); }
                }
            });
            XposedHelpers.findAndHookMethod(layoutClass, "onSectionChange", String.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam h) { try { View v=(View)labelField.get(h.thisObject); if(v!=null) v.setAlpha(0f); } catch(Throwable ignored) {} }
            });
            XposedHelpers.findAndHookMethod(layoutClass, "onLayout", boolean.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam h) { try { View v=(View)labelField.get(h.thisObject); if(v!=null) v.setAlpha(0f); } catch(Throwable ignored) {} }
            });
            XposedHelpers.findAndHookMethod(data, "refreshAdapterItem", String.class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    ArrayList<Object> old = null;
                    try {
                        @SuppressWarnings("unchecked") List<Object> source = (List<Object>) apps.get(null);
                        @SuppressWarnings("unchecked") ArrayList<Object> output = (ArrayList<Object>) target.get(null);
                        int cols = columns.getInt(null);
                        if (source == null || output == null || cols <= 0) return;
                        List<Object> snapshot = new ArrayList<>(source);
                        List<String> names = new ArrayList<>(snapshot.size());
                        for (Object element : snapshot) {
                            if (element == null || !item.isInstance(element)) return;
                            names.add((String) section.get(element));
                        }
                        List<Object> result = GroupSelector.select(snapshot, names, (String) param.args[0], cols);
                        if (result == null || result.isEmpty()) return;
                        List<String> selectedNames=new ArrayList<>();
                        for(Object o:result) selectedNames.add((String)section.get(o));
                        List<RowPlan.Row> rows=RowPlan.build(selectedNames,cols);
                        old = new ArrayList<>(output);
                        output.clear(); output.addAll(result);
                        refreshRows.invoke(Modifier.isStatic(refreshRows.getModifiers()) ? null : param.thisObject);
                        rowCount.setInt(null,rows.size()); active[0]=rows; currentSection[0]=(String)param.args[0];
                        param.setResult(null);
                    } catch (Throwable e) {
                        try { if (old != null) { @SuppressWarnings("unchecked") ArrayList<Object> out = (ArrayList<Object>) target.get(null); out.clear(); out.addAll(old); } } catch (Throwable ignored) { }
                        XposedBridge.log("OPPO Drawer Groups: fallback: " + e);
                    }
                }
            });
            XposedBridge.log("OPPO Drawer Groups: hook installed");
            
            // Hook COUITouchSearchView 调整右侧字母栏间距
            try {
                Class<?> touchSearchClass = p.classLoader.loadClass("com.coui.appcompat.touchsearchview.COUITouchSearchView");
                XposedHelpers.findAndHookMethod(touchSearchClass, "setItemSpacing", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        try {
                            // 间距调整为 8px（原厂为 4px）
                            int targetSpacing = 8;
                            param.args[0] = targetSpacing;
                        } catch (Throwable t) {
                            XposedBridge.log("OPPO Drawer spacing hook error: " + t);
                        }
                    }
                });
                
                XposedHelpers.findAndHookMethod(touchSearchClass, "onAttachedToWindow", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            int targetSpacing = 8;
                            Method setSpacing = touchSearchClass.getMethod("setItemSpacing", int.class);
                            setSpacing.invoke(param.thisObject, targetSpacing);
                        } catch (Throwable t) {
                            XposedBridge.log("OPPO Drawer onAttachedToWindow hook error: " + t);
                        }
                    }
                });
                XposedBridge.log("OPPO Drawer Groups: touchSearchView hook installed");
            } catch (Throwable t) {
                XposedBridge.log("OPPO Drawer Groups: failed to hook COUITouchSearchView: " + t);
            }
        } catch (Throwable e) { XposedBridge.log("OPPO Drawer Groups: unsupported launcher: " + e); }
    }
    private static Field field(Class<?> cls, String name) throws Exception {
        Field f = cls.getDeclaredField(name); f.setAccessible(true); return f;
    }
}
