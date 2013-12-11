package ly.count.android.api;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import ly.count.android.api.connectionqueue.ConnectionQueue;
import ly.count.android.api.eventqueue.EventQueue;

import org.OpenUDID.OpenUDID_manager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class Countly
{
	private static Countly sharedInstance_;
	private Timer timer_;
	private ConnectionQueue queue_;
	private EventQueue eventQueue_;
	private boolean isVisible_;
	private double unsentSessionLength_;
	private double lastTime_;

	static public Countly sharedInstance()
	{
		if (sharedInstance_ == null)
			sharedInstance_ = new Countly();
		
		return sharedInstance_;
	}
	
	private Countly()
	{
		queue_ = new ConnectionQueue();
		eventQueue_ = new EventQueue();
		timer_ = new Timer();
		timer_.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				onTimer();
			}
		}, 30 * 1000,  30 * 1000);

		isVisible_ = false;
		unsentSessionLength_ = 0;
	}
	
	public void init(Context context, String serverURL, String appKey)
	{
		OpenUDID_manager.sync(context);
		queue_.setContext(context);
		queue_.setServerURL(serverURL);
		queue_.setAppKey(appKey);
	}

	public void onStart()
	{
		lastTime_ = System.currentTimeMillis() / 1000.0;

		queue_.beginSession();

		isVisible_ = true;
	}
	
	public void onStop()
	{
		isVisible_ = false;

		if (eventQueue_.size() > 0)
			queue_.recordEvents(eventQueue_.events());		

		double currTime = System.currentTimeMillis() / 1000.0;
		unsentSessionLength_ += currTime - lastTime_;

		int duration = (int)unsentSessionLength_;
		queue_.endSession(duration);
		unsentSessionLength_ -= duration;
	}
	
	public void recordEvent(String key, int count)
	{
		eventQueue_.recordEvent(key, count);

		if (eventQueue_.size() >= 5)
			queue_.recordEvents(eventQueue_.events());
	}

	public void recordEvent(String key, int count, double sum)
	{
		eventQueue_.recordEvent(key, count, sum);

		if (eventQueue_.size() >= 5)
			queue_.recordEvents(eventQueue_.events());		
	}

	public void recordEvent(String key, Map<String, String> segmentation, int count)
	{
		eventQueue_.recordEvent(key, segmentation, count);
		
		if (eventQueue_.size() >= 5)
			queue_.recordEvents(eventQueue_.events());		
	}

	public void recordEvent(String key, Map<String, String> segmentation, int count, double sum)
	{
		eventQueue_.recordEvent(key, segmentation, count, sum);
		
		if (eventQueue_.size() >= 5)
			queue_.recordEvents(eventQueue_.events());		
	}
	
	private void onTimer()
	{
		if (isVisible_ == false)
			return;
		
		double currTime = System.currentTimeMillis() / 1000.0;
		unsentSessionLength_ += currTime - lastTime_;
		lastTime_ = currTime;
		
		int duration = (int)unsentSessionLength_;
		queue_.updateSession(duration);
		unsentSessionLength_ -= duration;

		if (eventQueue_.size() > 0)
			queue_.recordEvents(eventQueue_.events());		
	}
}