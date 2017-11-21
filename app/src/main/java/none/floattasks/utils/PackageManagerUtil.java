package none.floattasks.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import none.floattasks.MainApplication;

/**
 * Author: songyaru | songyaru9@gmail.com
 * Date: 2017/11/21  15:45
 */

public class PackageManagerUtil {
    private static final String AMAP_AUTO_PACKAGE_NAME = "com.autonavi.amapauto";
    private static final String AMAP_PACKAGE_NAME = "com.autonavi.minimap";
    private static final String BAIDU_PACKAGE_NAME = "com.baidu.baidumap";

    private boolean isAppInstalledByURL(String uri) {
        PackageManager pm = MainApplication.getInstance().getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public boolean isAppInstalled(String packageName) {
        final PackageManager packageManager = MainApplication.getInstance().getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        List<String> packageNameList = new ArrayList<>();
        if (packageInfoList != null) {
            for (PackageInfo packageInfo : packageInfoList) {
                String name = packageInfo.packageName.toLowerCase();
                packageNameList.add(name);
            }
        }
        return packageNameList.contains(packageName.toLowerCase());
    }

    public boolean haveAutoAmap() {
        return isAppInstalled(AMAP_AUTO_PACKAGE_NAME);
    }

    public boolean haveAmap() {
        return isAppInstalled(AMAP_PACKAGE_NAME);
    }

    public boolean haveBaiduMap() {
        return isAppInstalled(BAIDU_PACKAGE_NAME);
    }
}
