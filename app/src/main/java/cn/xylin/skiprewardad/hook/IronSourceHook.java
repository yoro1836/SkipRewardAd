package cn.xylin.skiprewardad.hook;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class IronSourceHook extends BaseHook {
    private Object listener;
    private boolean isHooked = false;

    public IronSourceHook(Context ctx) {
        super(ctx);
    }

    @Override
    protected void runHook() throws Throwable {
        tryHook(context.getClassLoader());

        if (!isHooked) {
            XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (isHooked) return;
                    if (param.hasThrowable()) return;

                    String className = (String) param.args[0];
                    if ("com.ironsource.mediationsdk.IronSource".equals(className)) {
                        log("IronSource class detected via detected loadClass. Initializing hooks...");
                        tryHook((ClassLoader) param.thisObject);
                    }
                }
            });
        }
    }

    private void tryHook(ClassLoader classLoader) {
        if (isHooked) return;

        Class<?> ironSourceClass = XposedHelpers.findClassIfExists("com.ironsource.mediationsdk.IronSource", classLoader);
        Class<?> listenerClass = XposedHelpers.findClassIfExists("com.ironsource.mediationsdk.sdk.RewardedVideoListener", classLoader);
        Class<?> placementClass = XposedHelpers.findClassIfExists("com.ironsource.mediationsdk.model.Placement", classLoader);

        if (ironSourceClass == null || listenerClass == null) {
            return;
        }

        XposedBridge.hookAllMethods(ironSourceClass, "setRewardedVideoListener", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length > 0 && listenerClass.isInstance(param.args[0])) {
                    listener = param.args[0];
                    log("IronSource listener captured!");
                }
            }
        });

        XposedBridge.hookAllMethods(ironSourceClass, "showRewardedVideo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (listener != null) {
                    log("IronSource showRewardedVideo intercepted. Skipping ad...");

                    Object fakePlacement = null;
                    if (placementClass != null) {
                        fakePlacement = XposedHelpers.newInstance(placementClass, "Reward", "VirtualItem", 1);
                    }

                    callMethod(listener, "onRewardedVideoAdOpened");
                    callMethod(listener, "onRewardedVideoAdRewarded", fakePlacement);
                    callMethod(listener, "onRewardedVideoAdClosed");

                    param.setResult(null);
                } else {
                    log("IronSource listener is null. Cannot skip.");
                }
            }
        });

        isHooked = true;
        log("IronSourceHook setup completed.");
    }

    @Override
    protected String targetPackageName() {
        return null;
    }

    @Override
    protected boolean isTarget() {
        return true;
    }
}
