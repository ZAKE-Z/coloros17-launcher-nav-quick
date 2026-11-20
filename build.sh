#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
export LC_ALL=C.UTF-8
ROOT=/mise/extra-tools/android-reverse
JAVA="$ROOT/usr/lib/jvm/java-21-openjdk-amd64/bin"
export LD_LIBRARY_PATH="$ROOT/usr/lib/x86_64-linux-gnu/android:$ROOT/usr/lib/x86_64-linux-gnu:$ROOT/usr/lib/7zip:$ROOT/usr/lib"
mkdir -p build/stubs build/classes build/dex build/tests build/out
"$JAVA/javac" --release 8 -d build/stubs $(find stubs -name '*.java')
"$JAVA/javac" --release 8 -cp build/stubs:"$ROOT/android.jar" -d build/classes $(find src -name '*.java')
"$JAVA/javac" --release 8 -cp build/classes:build/stubs -d build/tests $(find tests -name '*.java')
"$JAVA/java" -cp build/classes:build/tests GroupSelectorTest
"$JAVA/java" -cp build/classes:build/tests RowPlanTest
"$JAVA/java" -cp build/classes:build/stubs:build/tests:"$ROOT/android.jar" HookTest
"$JAVA/java" -cp "$ROOT/r8.jar" com.android.tools.r8.D8 --min-api 26 --output build/dex $(find build/classes -name '*.class')
"$ROOT/usr/bin/aapt" package -f -M AndroidManifest.xml -S res -I "$ROOT/usr/share/android-framework-res/framework-res.apk" -A assets -F build/unsigned.apk
(cd build/dex && zip -q -u ../unsigned.apk classes.dex)
"$ROOT/usr/bin/zipalign" -f 4 build/unsigned.apk build/aligned.apk
if [ ! -f build/signing-key.p12 ]; then
  "$JAVA/keytool" -genkeypair -keystore build/signing-key.p12 -storetype PKCS12 -storepass android -keypass android -alias drawer -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=OPPO Drawer Groups Local Build' >/dev/null 2>&1
fi
"$JAVA/java" -jar "$ROOT/usr/share/java/apksigner.jar" sign --ks build/signing-key.p12 --ks-pass pass:android --key-pass pass:android --out build/out/OPPO-Drawer-Groups.apk build/aligned.apk
"$JAVA/java" -jar "$ROOT/usr/share/java/apksigner.jar" verify --verbose --min-sdk-version 26 build/out/OPPO-Drawer-Groups.apk
sha256sum build/out/OPPO-Drawer-Groups.apk
