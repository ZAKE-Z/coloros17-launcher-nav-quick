package de.robv.android.xposed;
public abstract class XC_MethodHook {
    public final class Unhook {}
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}
    public final void testBefore(MethodHookParam p) throws Throwable { beforeHookedMethod(p); }
    public static class MethodHookParam { public Object thisObject; public Object[] args; public boolean skipped; public Object result; public void setResult(Object result) { skipped = true; this.result=result; } public Object getResult() { return result; } }
}
