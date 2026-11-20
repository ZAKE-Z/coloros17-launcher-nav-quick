package com.android.launcher3.allapps.cluster;
import java.util.*;
public class ClusterLayoutData {
    public static List<Object> mApps;
    public static ArrayList<Object> mAdapterItems = new ArrayList<>();
    public static int mNumColumn = 4;
    public static int mRawCount;
    public static float mItemHeight = 100;
    public static boolean fail;
    public static Object mLayout;
    private static float getItemHeight() { return 1; }
    private static float getLayoutBeyondMargin() { return 0; }
    private static void refreshRawMum() { if (fail) throw new RuntimeException("test"); mRawCount = (mAdapterItems.size()+mNumColumn-1)/mNumColumn; }
    private static void refreshAdapterItem(String s) {}
}
