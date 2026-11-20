package de.robv.android.xposed;
public final class XposedHelpers { public static XC_MethodHook hook; public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String name, Object... args) { hook = (XC_MethodHook) args[args.length - 1]; return null; } }
