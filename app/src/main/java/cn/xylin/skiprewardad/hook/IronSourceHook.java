package cn.xylin.skiprewardad.hook;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class IronSourceHook extends BaseHook {
    private Object listener;

    public IronSourceHook(Context ctx) {
        super(ctx);
    }

    @Override
    protected void runHook() throws Throwable {
        claza = findClass("com.ironsource.mediationsdk.IronSource");
        clazb = findClass("com.ironsource.mediationsdk.sdk.RewardedVideoListener");
        Class<?> placementClass = findClass("com.ironsource.mediationsdk.model.Placement");

        if (claza == null || clazb == null) {
            log("IronSource class or listener not found.");
            return;
        }

        XposedBridge.hookAllMethods(claza, "setRewardedVideoListener", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length > 0 && clazb.isInstance(param.args[0])) {
                    listener = param.args[0];
                    log("IronSource listener captured.");
                }
            }
        });

        XposedBridge.hookAllMethods(claza, "showRewardedVideo", new XC_MethodHook() {
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
                    log("IronSource listener is null.");
                }
            }
        });
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
