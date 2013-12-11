package ly.count.android.api.connectionqueue;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;

import ly.count.android.api.deviceinfo.DeviceInfo;

import org.OpenUDID.OpenUDID_manager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

public class ConnectionQueue {
	private ConcurrentLinkedQueue<String> queue_ = new ConcurrentLinkedQueue<String>();
	private Thread thread_ = null;
	private String appKey_;
	private Context context_;
	private String serverURL_;
	
	public void setAppKey(String appKey)
	{
		appKey_ = appKey;
	}

	public void setContext(Context context)
	{
		context_ = context;
	}
	
	public void setServerURL(String serverURL)
	{
		serverURL_ = serverURL;
	}
	
	public void beginSession()
	{
		String data;
		data  =       "app_key=" + appKey_;
		data += "&" + "device_id=" + DeviceInfo.getUDID();
		data += "&" + "timestamp=" + (long)(System.currentTimeMillis() / 1000.0);
		data += "&" + "sdk_version=" + "1.0";
		data += "&" + "begin_session=" + "1";
		data += "&" + "metrics=" + DeviceInfo.getMetrics(context_);
		
		queue_.offer(data);		
	
		tick();
	}

	public void updateSession(int duration)
	{
		String data;
		data  =       "app_key=" + appKey_;
		data += "&" + "device_id=" + DeviceInfo.getUDID();
		data += "&" + "timestamp=" + (long)(System.currentTimeMillis() / 1000.0);
		data += "&" + "session_duration=" + duration;

		queue_.offer(data);		

		tick();
	}
	
	public void endSession(int duration)
	{
		String data;
		data  =       "app_key=" + appKey_;
		data += "&" + "device_id=" + DeviceInfo.getUDID();
		data += "&" + "timestamp=" + (long)(System.currentTimeMillis() / 1000.0);
		data += "&" + "end_session=" + "1";
		data += "&" + "session_duration=" + duration;

		queue_.offer(data);		
		
		tick();
	}
	
	public void recordEvents(String events)
	{
		String data;
		data  =       "app_key=" + appKey_;
		data += "&" + "device_id=" + DeviceInfo.getUDID();
		data += "&" + "timestamp=" + (long)(System.currentTimeMillis() / 1000.0);
		data += "&" + "events=" + events;

		queue_.offer(data);		
		
		tick();		
	}
	
	private void tick()
	{
		if (thread_ != null && thread_.isAlive())
			return;
		
		if (queue_.isEmpty())
			return;
				
		thread_ = new Thread() 
		{
			@Override
			public void run()
			{
				while (true)
				{
					String data = queue_.peek();

					if (data == null)
						break;
					
					int index = data.indexOf("REPLACE_UDID");
					if (index != -1)
					{
						if (OpenUDID_manager.isInitialized() == false)
							break;						
						data.replaceFirst("REPLACE_UDID", OpenUDID_manager.getOpenUDID());						
					}
					
					try
					{
						DefaultHttpClient httpClient = new DefaultHttpClient();
						HttpGet method = new HttpGet(new URI(serverURL_ + "/i?" + data));			
						HttpResponse response = httpClient.execute(method);
						InputStream input = response.getEntity().getContent();
						while (input.read() != -1)
							;
						httpClient.getConnectionManager().shutdown();
												
						Log.d("Countly", "ok ->" + data);

						queue_.poll();
					}
					catch (Exception e)
					{
						Log.d("Countly", e.toString());
						Log.d("Countly", "error ->" + data);
						break;
					}
				}
			}
		};

		thread_.start();
	}
}
