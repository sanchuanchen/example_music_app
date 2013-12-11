package ly.count.android.api.eventqueue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import ly.count.android.api.event.Event;

public class EventQueue {
	private ArrayList<Event> events_;

	public EventQueue()
	{
		events_ = new ArrayList<Event>();		
	}

	public int size()
	{
		synchronized(this)
		{
			return events_.size();
		}
	}
	
	public String events()
	{
		String result = "[";

		synchronized (this)
		{
			for (int i = 0; i < events_.size(); ++i)
			{
				Event event = events_.get(i);
				
				result += "{";

				result += "\"" + "key" + "\"" + ":" + "\"" + event.key + "\"";
				
				if (event.segmentation != null)
				{
					String segmentation = "{";
					
					String keys[] = event.segmentation.keySet().toArray(new String[0]);
					
					for (int j = 0; j < keys.length; ++j)
					{
						String key = keys[j];
						String value = event.segmentation.get(key);
						
						segmentation += "\"" + key + "\"" + ":" + "\"" + value + "\"";

						if (j + 1 < keys.length)
							segmentation += ",";
					}
					
					segmentation += "}";

					result += "," + "\"" + "segmentation" + "\"" + ":" + segmentation;
				}
				
				result += "," + "\"" + "count" + "\"" + ":" + event.count;
				
				if (event.sum > 0)
					result += "," + "\"" + "sum" + "\"" + ":" + event.sum;
				
				result += "," + "\"" + "timestamp" + "\"" + ":" + (long)event.timestamp;

				result += "}";
		           
				if (i + 1 < events_.size())
					result += ",";
			}
			
			events_.clear();
		}

		result += "]";
		
		try
		{
			result = java.net.URLEncoder.encode(result, "UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			
		}

		return result;
	}
	
	public void recordEvent(String key, int count)
	{
		synchronized(this)
		{
			for (int i = 0; i < events_.size(); ++i)
			{
				Event event = events_.get(i);
				
				if (event.key.equals(key))
				{
					event.count += count;
					event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
					return;
				}
			}
			
			Event event = new Event();
			event.key = key;
			event.count = count;
			event.timestamp = System.currentTimeMillis() / 1000.0;
			events_.add(event);
		}
	}

	public void recordEvent(String key, int count, double sum)
	{
		synchronized(this)
		{
			for (int i = 0; i < events_.size(); ++i)
			{
				Event event = events_.get(i);
				
				if (event.key.equals(key))
				{
					event.count += count;
					event.sum += sum;
					event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
					return;
				}
			}
			
			Event event = new Event();
			event.key = key;
			event.count = count;
			event.sum = sum;
			event.timestamp = System.currentTimeMillis() / 1000.0;
			events_.add(event);
		}		
	}

	public void recordEvent(String key, Map<String, String> segmentation, int count)
	{
		synchronized(this)
		{
			for (int i = 0; i < events_.size(); ++i)
			{
				Event event = events_.get(i);
				
				if (event.key.equals(key) &&
					event.segmentation != null && event.segmentation.equals(segmentation))
				{
					event.count += count;
					event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
					return;
				}
			}
			
			Event event = new Event();
			event.key = key;
			event.segmentation = segmentation;
			event.count = count;
			event.timestamp = System.currentTimeMillis() / 1000.0;
			events_.add(event);
		}		
	}

	public void recordEvent(String key, Map<String, String> segmentation, int count, double sum)
	{
		synchronized(this)
		{
			for (int i = 0; i < events_.size(); ++i)
			{
				Event event = events_.get(i);
				
				if (event.key.equals(key) &&
					event.segmentation != null && event.segmentation.equals(segmentation))
				{
					event.count += count;
					event.sum += sum;
					event.timestamp = (event.timestamp + (System.currentTimeMillis() / 1000.0)) / 2;
					return;
				}
			}
			
			Event event = new Event();
			event.key = key;
			event.segmentation = segmentation;
			event.count = count;
			event.sum = sum;
			event.timestamp = System.currentTimeMillis() / 1000.0;
			events_.add(event);
		}
	}	
}
