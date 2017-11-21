package none.floattasks;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import none.floattasks.utils.Debounce;
import none.floattasks.utils.Util;

public class SystemOverlayMenuService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private FloatingActionButton floatingActionButton;

    private FloatingActionButton.Builder floatingActionButtonBuilder;

    private FloatingActionMenu floatingActionMenu;

    private WindowManager windowManager;

    private Debounce<String> hideDebounce;

    public boolean serviceWillBeDismissed;

    public SystemOverlayMenuService() {
    }

    public class LocalBinder extends Binder {
        SystemOverlayMenuService getService() {
            return SystemOverlayMenuService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        serviceWillBeDismissed = false;

        initDebounce();

        initFloatActionButton();
        initFloatActionMenu();

        windowManager = floatingActionButton.getWindowManager();
        floatingActionButton.setOnTouchListener(new View.OnTouchListener() {
            // 触屏监听
            float lastX, lastY;
            int oldOffsetX, oldOffsetY;
            int tag = 0;// 悬浮球 所需成员变量

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setFloatingActionButtonVisible(floatingActionButtonBuilder);
                final int action = event.getAction();
                WindowManager.LayoutParams params =
                    (WindowManager.LayoutParams) floatingActionButtonBuilder.getLayoutParams();
                float x = event.getX();
                float y = event.getY();
                if (tag == 0) {
                    oldOffsetX = params.x; // 偏移量
                    oldOffsetY = params.y; // 偏移量
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    hideMenu(true);
                    params.x += (int) (x - lastX) / 3; // 减小偏移量,防止过度抖动
                    params.y += (int) (y - lastY) / 3; // 减小偏移量,防止过度抖动
                    tag = 1;

                    floatingActionButtonBuilder.setLayoutParams(params);
                    windowManager.updateViewLayout(floatingActionButton, params);

                } else if (action == MotionEvent.ACTION_UP) {
                    int newOffsetX = params.x;
                    int newOffsetY = params.y;
                    // 只要按钮一动位置不是很大,就认为是点击事件
                    if (Math.abs(oldOffsetX - newOffsetX) <= 20 && Math.abs(oldOffsetY - newOffsetY) <= 20) {
                        floatingActionButton.performClick();
                    } else {
                        tag = 0;
                    }
                }
                hideDebounce.call("HIDE_BUTTON");
                return true;
            }
        });

        floatingActionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {

            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                if (serviceWillBeDismissed) {
                    SystemOverlayMenuService.this.stopSelf();
                    serviceWillBeDismissed = false;
                }
            }
        });
    }

    private void initDebounce() {
        Debounce.Callback<String> hideBtnCallback = new Debounce.Callback<String>() {
            @Override
            public void call(String arg) {
                if (floatingActionButton != null) {
                    Util.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setFloatingActionButtonInVisible(floatingActionButtonBuilder);
                        }
                    });
                }
            }
        };
        hideDebounce = new Debounce<>(hideBtnCallback, 10);
    }

    private void initFloatActionButton() {
        int actionButtonSize = getResources().getDimensionPixelSize(R.dimen.red_action_button_size);

        int actionButtonContentSize = getResources().getDimensionPixelSize(R.dimen.red_action_button_content_size);
        int actionButtonContentMargin = getResources().getDimensionPixelSize(R.dimen.red_action_button_content_margin);

        ImageView iconStar = new ImageView(this);
        iconStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_important));

        FrameLayout.LayoutParams iconStarParams =
            new FloatingActionButton.LayoutParams(actionButtonContentSize, actionButtonContentSize);
        iconStarParams.setMargins(actionButtonContentMargin, actionButtonContentMargin, actionButtonContentMargin,
            actionButtonContentMargin);

        final WindowManager.LayoutParams params = FloatingActionButton.Builder.getDefaultSystemWindowParams(this);
        params.width = actionButtonSize;
        params.height = actionButtonSize;
        params.alpha = 1.0f;

        floatingActionButtonBuilder = new FloatingActionButton.Builder(this);
        floatingActionButton =
            floatingActionButtonBuilder.setSystemOverlay(true).setContentView(iconStar, iconStarParams)
                .setBackgroundDrawable(R.drawable.button_action_red_selector)
                .setPosition(FloatingActionButton.POSITION_TOP_CENTER).setLayoutParams(params).build();

        hideDebounce.call("HIDE_BUTTON");
    }

    private void initFloatActionMenu() {
        int actionMenuRadius = getResources().getDimensionPixelSize(R.dimen.red_action_menu_radius);
        int subActionButtonSize = getResources().getDimensionPixelSize(R.dimen.blue_sub_action_button_size);
        int subActionButtonContentMargin =
            getResources().getDimensionPixelSize(R.dimen.blue_sub_action_button_content_margin);
        SubActionButton.Builder subButtonBuilder = new SubActionButton.Builder(this);
        subButtonBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_action_blue_selector));

        SubActionButton.Builder closeButtonBuilder = new SubActionButton.Builder(this);
        closeButtonBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_action_red_selector));

        FrameLayout.LayoutParams subContentParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        subContentParams
            .setMargins(subActionButtonContentMargin, subActionButtonContentMargin, subActionButtonContentMargin,
                subActionButtonContentMargin);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(subActionButtonSize, subActionButtonSize);
        subButtonBuilder.setLayoutParams(layoutParams);
        closeButtonBuilder.setLayoutParams(layoutParams);

        ImageView callIcon = new ImageView(this);
        ImageView homeIcon = new ImageView(this);
        ImageView closeIcon = new ImageView(this);
        ImageView mapIcon = new ImageView(this);

        callIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_phone));
        homeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home));
        closeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_cancel));
        mapIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_place));

        SubActionButton callButton = subButtonBuilder.setContentView(callIcon, subContentParams).build();
        SubActionButton homeButton = subButtonBuilder.setContentView(homeIcon, subContentParams).build();
        SubActionButton closeButton = closeButtonBuilder.setContentView(closeIcon, subContentParams).build();
        SubActionButton mapButton = subButtonBuilder.setContentView(mapIcon, subContentParams).build();

        floatingActionMenu = new FloatingActionMenu.Builder(this, true)
                                 .addSubActionView(mapButton, mapButton.getLayoutParams().width,
                                     mapButton.getLayoutParams().height)
                                 .addSubActionView(callButton, callButton.getLayoutParams().width,
                                     callButton.getLayoutParams().height)
                                 .addSubActionView(homeButton, homeButton.getLayoutParams().width,
                                     homeButton.getLayoutParams().height)
                                 .addSubActionView(closeButton, closeButton.getLayoutParams().width,
                                     closeButton.getLayoutParams().height).setRadius(actionMenuRadius).setStartAngle(0)
                                 .setEndAngle(180).attachTo(floatingActionButton).build();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceWillBeDismissed = true;
                hideMenu();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);

            }
        });
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, null);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appName = getString(R.string.app_name);
                String url = "androidauto://rootmap?sourceApplication=" + appName;
                Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse(url));
                intent.setPackage("com.autonavi.amapauto");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                hideMenu(true);
            }
        });

    }

    public void setFloatingActionButtonVisible(final FloatingActionButton.Builder builder) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) builder.getLayoutParams();
        if (params.alpha == 1.0f) {
            return;
        }
        params.alpha = 1.0f;
        builder.setLayoutParams(params);
        windowManager.updateViewLayout(floatingActionButton, params);
    }

    public void setFloatingActionButtonInVisible(final FloatingActionButton.Builder builder) {
        setFloatingActionButtonAlpha(builder, 0.3f);
        hideMenu(true);
    }

    private void setFloatingActionButtonAlpha(FloatingActionButton.Builder builder, float f) {
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) builder.getLayoutParams();
        params.alpha = f;
        builder.setLayoutParams(params);
        windowManager.updateViewLayout(floatingActionButton, params);
    }

    private void hideMenu() {
        hideMenu(false);
    }

    private void hideMenu(boolean flag) {
        if (floatingActionMenu != null && floatingActionMenu.isOpen()) {
            floatingActionMenu.close(flag);
        }
    }

    @Override
    public void onDestroy() {
        hideMenu();

        if (floatingActionButton != null) {
            floatingActionButton.detach();
            floatingActionButton = null;
        }

        hideDebounce.terminate();

        super.onDestroy();
    }

}

