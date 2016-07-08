package net.gini.android.vision.testhelper;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;

public class Util {

    public static <T> Intent createIntent(Class<T> klass) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        String targetPackage = instrumentation.getTargetContext().getPackageName();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(targetPackage, klass.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static void prepareLooper() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }
}
