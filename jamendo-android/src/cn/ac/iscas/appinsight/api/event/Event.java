package cn.ac.iscas.appinsight.api.event;

import java.util.Map;

public class Event {
	public String key = null;
	public Map<String, String> segmentation = null;
	public int count = 0;
	public double sum = 0;
	public double timestamp = 0;
}
