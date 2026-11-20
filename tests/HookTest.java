import java.util.*;
import org.oppo.drawergroups.Entry;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem;
import com.android.launcher3.allapps.cluster.ClusterLayoutData;
public class HookTest {
    static void check(boolean b) { if (!b) throw new AssertionError(); }
    public static void main(String[] args) throws Throwable {
        XC_LoadPackage.LoadPackageParam p = new XC_LoadPackage.LoadPackageParam();
        p.packageName="com.android.launcher"; p.processName=p.packageName; p.classLoader=HookTest.class.getClassLoader();
        new Entry().handleLoadPackage(p);
        check(XposedHelpers.hook != null);
        List<Object> list = new ArrayList<>();
        for (String s : new String[]{"L","M","N"}) for(int i=0;i<7;i++) list.add(new AdapterItem(s));
        ClusterLayoutData.mApps=list;
        XC_MethodHook.MethodHookParam h = new XC_MethodHook.MethodHookParam();
        h.thisObject=null; h.args=new Object[]{"M"};
        XposedHelpers.hook.testBefore(h);
        check(h.skipped && ClusterLayoutData.mAdapterItems.size()==21 && ClusterLayoutData.mRawCount==6);
        ClusterLayoutData.mAdapterItems.clear(); ClusterLayoutData.mAdapterItems.add(list.get(0));
        ClusterLayoutData.fail=true; h=new XC_MethodHook.MethodHookParam(); h.thisObject=new ClusterLayoutData(); h.args=new Object[]{"M"};
        XposedHelpers.hook.testBefore(h);
        check(!h.skipped && ClusterLayoutData.mAdapterItems.size()==1 && ClusterLayoutData.mAdapterItems.get(0)==list.get(0));
        System.out.println("hook success and failure fallback tests passed");
    }
}
