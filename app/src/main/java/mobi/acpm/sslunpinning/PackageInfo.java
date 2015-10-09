package mobi.acpm.sslunpinning;

import android.graphics.drawable.Drawable;

/**
 * Created by acpm on 04/10/15.
 */
public class PackageInfo {

    public String appName = "";
    public String packName = "";
    public boolean bypassed = false;
    public Drawable icon;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPckName() {
        return packName;
    }

    public void setPckName(String packName) {
        this.packName = packName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isBypassed() {
        return bypassed;
    }

    public void setBypassed(boolean bypassed) {
        this.bypassed = bypassed;
    }
}

