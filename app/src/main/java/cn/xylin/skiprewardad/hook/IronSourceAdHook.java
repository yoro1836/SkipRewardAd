package cn.xylin.skiprewardad.hook;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class IronSourceAdHook extends BaseHook {
    private Object listener, ironSourceAd, defaultReward;

    public IronSourceAdHook(Context ctx) {
        super(ctx);
    }

    @Override
    protected void runHook() throws Throwable {
        claza = findClass("com.ironsource.mediationsdk.IronSource");
        if (claza == null) {
            return;
        }

        try {
            // 기본 보상 객체를 만듭니다 (필요에 따라 조정할 수 있음)
            defaultReward = new Object(); // IronSource SDK에 맞는 적절한 보상 객체를 만들어야 합니다.
        } catch (Throwable ignore) {
            return;
        }

        XposedBridge.hookAllMethods(claza, "setRewardedVideoListener", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length == 1 && param.args[0] != null) {
                    listener = param.args[0];
                    if (isHooked(listener.getClass().getName())) {
                        return;
                    }

                    XposedBridge.hookAllMethods(listener.getClass(), "onRewardedVideoAdLoaded", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // 광고가 로드될 때 호출됨
                        }
                    });
                }
            }
        });

        XposedBridge.hookAllMethods(claza, "showRewardedVideo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (listener != null) {
                    // 보상 이벤트를 강제로 트리거합니다
                    callMethod(listener, "onRewardedVideoAdOpened");
                    callMethod(listener, "onRewardedVideoAdStarted");
                    callMethod(listener, "onRewardedVideoAdRewarded", defaultReward);
                    callMethod(listener, "onRewardedVideoAdEnded");
                    callMethod(listener, "onRewardedVideoAdClosed");
                    param.setResult(null);
                    log("IronSourceAd - 보상 지급 완료");
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
