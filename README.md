# OPPO Drawer Groups / OPPO 应用抽屉导航优化

一个面向 OPPO / OnePlus（ColorOS / OxygenOS）系统桌面的 Xposed 模块，用于改造应用抽屉（App Drawer）的字母导航：

1. **相邻字母分组显示**：点击右侧字母索引时，抽屉不再只显示该字母的应用，而是同时显示该字母与前后最近的非空字母分组，并以分组形式排版。
2. **分组字母标签**：在每个分组第一行的左侧绘制分组字母，隐藏系统自带的分组标题。
3. **可调字母栏间距**：右侧 A–Z 快速滑动字母栏（`COUITouchSearchView`）的字母间距可通过一个常量调整（原厂为 `4px`）。

An Xposed module for OPPO / OnePlus (ColorOS / OxygenOS) stock launchers that reshapes the app drawer's alphabet navigation:

1. **Neighbor-letter grouping**: tapping a letter in the right-side index shows that letter *and* the nearest non-empty letter groups before and after it, laid out as labeled groups — instead of filtering to a single letter.
2. **Group labels**: draws the group's letter to the left of the first row of each group and hides the system's own section header.
3. **Adjustable index spacing**: the A–Z fast-scroll letter bar (`COUITouchSearchView`) letter spacing is controlled by a single constant (stock value is `4px`).

---

## 目标环境 / Target environment

| 项目 / Item | 值 / Value |
| --- | --- |
| 包名 / Package | `com.android.launcher` |
| 桌面版本 / Launcher version | `16.6.11` (Android 16)、`17.3.12` (Android 17) |
| 已验证机型 / Verified device | OPPO Find X8 Pro (`PKC110`) |
| 框架 / Framework | Xposed 兼容框架（LSPosed / Vector 等）/ any Xposed-compatible framework |
| minSdk / targetSdk | 26 / 35 |

> ⚠️ 该模块通过反射调用桌面内部 API（`com.android.launcher3.allapps.cluster.*`、`com.coui.appcompat.touchsearchview.COUITouchSearchView`）。系统版本或桌面版本不同，内部字段和方法名可能变化，模块会尽量回退到原始逻辑并记录 `OPPO Drawer Groups` 日志。不同机型的兼容性需自行验证。
>
> ⚠️ This module reflects on launcher-internal APIs. Field/method names may differ across system or launcher versions; the module falls back to original behavior and logs under the `OPPO Drawer Groups` tag when reflection fails. Compatibility on other devices is not guaranteed.

---

## 功能说明 / How it works

- `GroupSelector.select()`：根据选中的字母，选出该字母及前后最近的非空字母分组对应的应用项。
- `RowPlan.build()`：把选中的应用按字母分组规划成行（每组可跨多行）。
- `Entry.java`：Hook 桌面内部方法：
  - `ClusterLayoutData.refreshAdapterItem`：替换应用列表为分组结果；
  - `refreshOffsetY` / `layoutSingleRaw` / `getIconLayoutHeight`：压缩行高、加入组间距；
  - `OplusClusterIconLayout.dispatchDraw`：在每组首行左侧绘制字母标签；
  - `COUITouchSearchView.setItemSpacing`：修改右侧字母栏间距（见下）。

- `GroupSelector.select()`: picks app items for the selected letter plus the nearest non-empty neighbor groups.
- `RowPlan.build()`: plans selected apps into rows grouped by letter (a group may span multiple rows).
- `Entry.java`: hooks internal launcher methods to swap the app list, adjust row/group spacing, draw group labels, and tweak the right-side letter bar spacing.

### 两个构建版本 / Two build variants

仓库只提供两个版本，没有中间过渡版本。运行 `bash build.sh` 会同时生成：

Two variants only — no intermediate builds. `bash build.sh` produces both:

| 版本 / Variant | APK | 字母栏间距 / Letter bar spacing |
| --- | --- | --- |
| 原始间距版 / Stock spacing | `OPPO-Drawer-Groups-StockSpacing.apk` | 不修改，保持原厂 `4px` / untouched, stock `4px` |
| 间距修改版 / Spacing modified | `OPPO-Drawer-Groups-Spacing8px.apk` | Hook `setItemSpacing` 改为 `8px` / hooks `setItemSpacing` to `8px` |

两者共用同一个包名 `org.oppo.drawergroups`，**只能二选一安装**。两者字母分组功能完全一致，唯一区别是是否调整右侧 A–Z 字母栏间距。

Both variants share the same package name `org.oppo.drawergroups` — **install only one**. Grouping behavior is identical; the only difference is whether the right-side A–Z letter bar spacing is adjusted.

### 调整间距数值 / Changing the spacing value

间距由 `src/org/oppo/drawergroups/BuildConfig.java` 的 `SPACING_PX` 常量控制（**物理像素 px，不是 dp，不要乘以 density / raw pixels, not dp — do not multiply by density**）。原厂为 `4px`，间距修改版默认 `8px`。修改后重新构建即可。间距 Hook 的开关是同文件的 `ENABLE_SPACING_HOOK`。

The spacing is controlled by `SPACING_PX` in `src/org/oppo/drawergroups/BuildConfig.java` (raw pixels, not dp). Stock is `4px`; the spacing variant defaults to `8px`. Rebuild after changing. The hook on/off switch is `ENABLE_SPACING_HOOK` in the same file.

---

## 构建 / Build

### 通用思路 / General idea

1. 用 JDK 将 `src/` 编译为 Java 8 字节码，并针对 `stubs/` 与 Android SDK 的 `android.jar`。
2. 用 D8 将 class 转换为 DEX。
3. 用 `aapt` 打包 `AndroidManifest.xml` / `res/` / `assets/` 为未签名 APK，加入 `classes.dex`。
4. 用 `zipalign` 对齐，再用 `apksigner` 签名。

本仓库的 `build.sh` 假设工具链位于 `/mise/extra-tools/android-reverse`（本机 Docker 环境专用）。在其他机器上请按上一步替换为自己的 JDK / Android SDK build-tools 路径。

`build.sh` expects the toolchain at `/mise/extra-tools/android-reverse` (specific to the author's Docker environment). On other machines, replace those paths with your own JDK / Android SDK build-tools.

```bash
bash build.sh
```

产物输出 / Output:
- `build/out/OPPO-Drawer-Groups-StockSpacing.apk`（原始间距 / stock spacing）
- `build/out/OPPO-Drawer-Groups-Spacing8px.apk`（间距修改 / spacing 8px）

`assets/xposed_init` 声明入口类 `org.oppo.drawergroups.Entry`。

> `build/signing-key.p12` 是本地测试签名密钥，**仅用于本机调试**，请勿作为正式密钥，也不要对外分发。
>
> `build/signing-key.p12` is a **local test-only signing key**. Do not use it as an official key or distribute it.

---

## 安装与使用 / Install & use

1. 从上述两个版本中**任选一个**安装（无启动界面）。/ Install **one** of the two APKs above (no launcher UI).
2. 在 Xposed 框架管理器中启用本模块，**仅勾选系统桌面** `com.android.launcher`。
3. 按框架提示重启桌面进程或手机，打开应用抽屉查看效果。

1. Install `build/out/OPPO-Drawer-Groups.apk` (no launcher UI).
2. Enable the module in your Xposed framework manager, selecting **only** `com.android.launcher`.
3. Restart the launcher (or reboot) as instructed by the framework, then open the app drawer.

**回退 / Rollback**：先在框架管理器中禁用模块并重启桌面/手机，再卸载本 APK。不要替换系统桌面 APK，也不要直接修改系统分区或引导分区。

**Rollback**: disable the module in the framework manager and restart the launcher/device, then uninstall the APK. Do not replace the system launcher APK or modify system/boot partitions directly.

---

## 免责声明 / Disclaimer

本模块为个人学习与定制用途，按“原样”提供，不附带任何担保。修改系统桌面可能导致桌面异常；使用前请备份数据并确保有可用的备用操作入口。作者不对任何设备损坏或数据丢失负责。

This module is provided "as is" for personal learning and customization, without warranty of any kind. Modifying the system launcher may cause instability; back up your data and ensure an alternative launcher/recovery path before use. The author is not responsible for any device damage or data loss.

---

## 许可 / License

见 [LICENSE](LICENSE)（MIT）。/ See [LICENSE](LICENSE) (MIT).
