package unife.icedroid.core;

import java.util.HashMap;

public class Intent {
	private HashMap<String, Object> data;
	
	public Intent() {
		data = new HashMap<>();
	}
	
	public void putExtra(String key, Object value) {
		data.put(key, value);
	}
	
	public Object getExtra(String key) {
		return data.get(key);
	}
	
	public boolean hasExtra(String key) {
		return (data.containsKey(key));
	}

}
