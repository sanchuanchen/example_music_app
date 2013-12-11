package ly.count.android.api.deviceinfo;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.OpenUDID.OpenUDID_manager;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DeviceInfo {
	public static String getUDID()
	{
		return OpenUDID_manager.isInitialized() == false ? "REPLACE_UDID" : OpenUDID_manager.getOpenUDID();
	}
	
	public static String getOS()
	{
		return "Android";
	}
	
	public static String getOSVersion()
	{
		return android.os.Build.VERSION.RELEASE;
	}

	public static String getDevice()
	{
		return android.os.Build.MODEL;
	}
	
	public static String getResolution(Context context)
	{
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		
		Display display = wm.getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		return metrics.heightPixels + "x" + metrics.widthPixels;
	}
	
	public static String getCarrier(Context context)
	{
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getNetworkOperatorName();
	}

	public static String getLocale()
	{
		Locale locale = Locale.getDefault();
		return locale.getLanguage() + "_" + locale.getCountry();
	}
	
	public static String appVersion(Context context)
	{
		String result = "1.0";
		try {
			result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}		

		return result;
	}

	public static String getMetrics(Context context)
	{
		String result = "{";
		
		result +=       "\"" + "_device"      + "\"" + ":" + "\"" + getDevice()            + "\"";
		
		result += "," + "\"" + "_os"          + "\"" + ":" + "\"" + getOS()                + "\"";
		
		result += "," + "\"" + "_os_version"  + "\"" + ":" + "\"" + getOSVersion()         + "\"";
		
		result += "," + "\"" + "_carrier"     + "\"" + ":" + "\"" + getCarrier(context)    + "\"";
		
		result += "," + "\"" + "_resolution"  + "\"" + ":" + "\"" + getResolution(context) + "\"";
		
		result += "," + "\"" + "_locale"      + "\"" + ":" + "\"" + getLocale()            + "\"";

		result += "," + "\"" + "_app_version" + "\"" + ":" + "\"" + appVersion(context)            + "\"";

		result += "}";
		
		try
		{
			result = java.net.URLEncoder.encode(result, "UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			
		}

		return result;
	}
}
