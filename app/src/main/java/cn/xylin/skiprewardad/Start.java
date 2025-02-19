package cn.xylin.skiprewardad;

import android.app.Application;
import android.content.Context;

import cn.xylin.skiprewardad.hook.ApplovinAdHook;
import cn.xylin.skiprewardad.hook.BaiduAdHook;
import cn.xylin.skiprewardad.hook.FusionAdHook;
import cn.xylin.skiprewardad.hook.GdtAdHook1;
import cn.xylin.skiprewardad.hook.GdtAdHook2;
import cn.xylin.skiprewardad.hook.GoogleAdHook1;
import cn.xylin.skiprewardad.hook.IronSourceAdHook; // 새로운 Hook 클래스 임포트
import cn.xylin.skiprewardad.hook.KsAdHook;
import cn.xylin.skiprewardad.hook.MintegralAdHook;
import cn.xylin.skiprewardad.hook.SigmobAdHook;
import cn.xylin.skiprewardad.hook.TTAdHook;
import cn.xylin.skiprewardad.hook.UnityAdHook1;
import cn.xylin.skiprewardad.hook.UnityAdHook2;
import cn.xylin.skiprewardad.hook.VungleAdHook;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Start implements IXposedHookLoadPackage {
    private int hash;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam load) {
        if (!load.isFirstApplication || !load.appInfo.processName.equals(load.processName)) {
            return;
        }
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (hash == hashCode()) {
                    return;
                }
                hash = hashCode();
                startHook((Context) param.args[0]);
            }
        });
    }
    
    private synchronized void startHook(Context baseContext) {
        new GdtAdHook1(baseContext);
        new GdtAdHook2(baseContext);
        new FusionAdHook(baseContext);
        new TTAdHook(baseContext);
        new BaiduAdHook(baseContext);
        new SigmobAdHook(baseContext);
        new MintegralAdHook(baseContext);
        new UnityAdHook1(baseContext);
        new UnityAdHook2(baseContext);
        new KsAdHook(baseContext);
        new VungleAdHook(baseContext);
        new GoogleAdHook1(baseContext);
        new ApplovinAdHook(baseContext);
        new IronSourceAdHook(baseContext); // 여기에 새로운 Hook 추가
    }
}