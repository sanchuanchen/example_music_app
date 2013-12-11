package org.OpenUDID;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;


public class OpenUDID_manager{
	public final static String PREF_KEY = "openudid";
	public final static String PREFS_NAME = "openudid_prefs";
	public final static String TAG = "OpenUDID";
	
	private final static boolean LOG = true; //Display or not debug message
	
	private final SharedPreferences mPreferences; //Preferences to store the OpenUDID
	private final Context mContext; //Application context
	
	private static String OpenUDID = null;
	private static boolean mInitialized = false; 
	
	
	private OpenUDID_manager(Context context) {
		mPreferences =  context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mContext = context;
	}
	
	/**
	 * store the openUDID
	 */
	
	private void storeOpenUDID() {
    	final Editor e = mPreferences.edit();
		e.putString(PREF_KEY, OpenUDID);
		e.commit();
	}
	
	/*
	 * Generate a new OpenUDID
	 */
	private void generateOpenUDID() {
		if (LOG) Log.d(TAG, "Generating openUDID");
		//Try to get the ANDROID_ID
		OpenUDID = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID); 
		if (OpenUDID == null || OpenUDID.equals("9774d56d682e549c") || OpenUDID.length() < 15 ) {
			//if ANDROID_ID is null, or it's equals to the GalaxyTab generic ANDROID_ID or bad, generates a new one
			final SecureRandom random = new SecureRandom();
			OpenUDID = new BigInteger(64, random).toString(16);
		}
    }

	/**
	 * The Method to call to get OpenUDID
	 * @return the OpenUDID
	 */
	public static String getOpenUDID() {
		if (!mInitialized) Log.e("OpenUDID", "Initialisation isn't done");
		return OpenUDID;
	}
	
	/**
	 * The Method to call to get OpenUDID
	 * @return the OpenUDID
	 */
	public static boolean isInitialized() {
		return mInitialized;
	}
	
	/**
	 * The Method the call at the init of your app
	 * @param context	you current context
	 */
	public static void sync(Context context) {
		//Initialise the Manager
		OpenUDID_manager manager = new OpenUDID_manager(context);
		
		//Try to get the openudid from local preferences
		OpenUDID = manager.mPreferences.getString(PREF_KEY, null);
		if (OpenUDID == null) //Not found
		{
			manager.generateOpenUDID();
			manager.storeOpenUDID();
		} else {//Got it, you can now call getOpenUDID()
			if (LOG) Log.d(TAG, "OpenUDID: " + OpenUDID);
		}
		mInitialized = true;
	}
}
