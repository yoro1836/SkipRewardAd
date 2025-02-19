package cn.xylin.skiprewardad.hook;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import com.ironsource.mediationsdk.IronSource; // IronSource 클래스 임포트
import com.ironsource.mediationsdk.LevelPlayRewardedVideoListener; // LevelPlayRewardedVideoListener 인터페이스 임포트
import com.ironsource.mediationsdk.Placement; // Placement 클래스 임포트
import com.ironsource.mediationsdk.AdInfo; // AdInfo 클래스 임포트

public class IronSourceAdHook extends BaseHook { // BaseHook 상속 추가 (BaseHook 클래스가 있는 경우)

    public IronSourceAdHook(Context context) {
        super(context); // BaseHook 생성자 호출 (BaseHook 클래스가 있는 경우)
        startHook(context);
    }

    private void startHook(Context context) {
        try {
            Class<?> ironSourceClass = XposedHelpers.findClass("com.ironsource.mediationsdk.IronSource", context.getClassLoader()); // 클래스 이름 수정
            String rewardAdMethodName = "showRewardedVideo";

            XposedHelpers.findAndHookMethod(ironSourceClass, rewardAdMethodName, String.class, new XC_MethodHook() { // 메소드 파라미터 타입 명시 (placementName: String)
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    android.util.Log.d("SkipRewardAd", "IronSource reward ad method " + rewardAdMethodName + " is hooked!");

                    // 1. 리스너 객체 얻기 (LevelPlayRewardedVideoListener)
                    LevelPlayRewardedVideoListener listener = (LevelPlayRewardedVideoListener) IronSource.getLevelPlayRewardedVideoListener(); // IronSource.getLevelPlayRewardedVideoListener() 사용

                    if (listener != null) {
                        // 2. 리워드 정보 생성 (Placement 객체)
                        Placement placement = IronSource.getRewardedVideoPlacementInfo((String) param.args[0]); // placementName 파라미터 사용
                        AdInfo adInfo = null; // AdInfo는 null 또는 가짜 객체로 설정 가능 (필요에 따라 수정)

                        // 3. onAdRewarded 콜백 직접 호출 (광고 시청 완료 보상 지급)
                        listener.onAdRewarded(placement, adInfo);
                        android.util.Log.d("SkipRewardAd", "IronSource onAdRewarded callback called directly!");

                        // 4. (선택 사항) onAdOpened, onAdClosed 콜백 호출 (광고 생명주기 이벤트 시뮬레이션)
                        listener.onAdOpened(adInfo);
                        listener.onAdClosed(adInfo);
                        android.util.Log.d("SkipRewardAd", "IronSource onAdOpened and onAdClosed callbacks called!");
                    } else {
                        android.util.Log.w("SkipRewardAd", "LevelPlayRewardedVideoListener is null. Reward callback may not be triggered.");
                    }

                    // 광고 표시 메소드 실행 취소 (광고 건너뛰기)
                    param.setResult(null); // or return null; (메소드 반환 타입에 따라 다름. void일 경우 setResult(null))
                    android.util.Log.d("SkipRewardAd", "IronSource showRewardedVideo method cancelled!");
                }
            });
            android.util.Log.d("SkipRewardAd", "IronSourceAdHook started successfully!");

        } catch (Throwable e) {
            android.util.Log.e("SkipRewardAd", "IronSourceAdHook failed to start: " + e.getMessage());
        }
    }

    @Override
    protected boolean isTarget() {
        return true; // or 특정 조건 (패키지 이름 등)
    }

    @Override
    protected String targetPackageName() {
        return null; // or 특정 패키지 이름
    }
}