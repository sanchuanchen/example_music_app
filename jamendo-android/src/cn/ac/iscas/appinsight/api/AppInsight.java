package cn.ac.iscas.appinsight.api;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import cn.ac.iscas.appinsight.api.connectionqueue.ConnectionQueue;
import cn.ac.iscas.appinsight.api.eventqueue.EventQueue;
import cn.ac.iscas.appinsight.api.udid.OpenUDID_manager;

public class AppInsight {
	private static AppInsight sharedInstance_;
	private Timer timer_;
	private ConnectionQueue queue_;
	private EventQueue eventQueue_;
	private boolean isVisible_;
	private double unsentSessionLength_;
	private double lastTime_;

	static public AppInsight sharedInstance()
	{
		if (sharedInstance_ == null)
			sharedInstance_ = new AppInsight();
		
		return sharedInstance_;
	}
	
	private AppInsight()
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
