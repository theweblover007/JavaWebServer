package com.javaprophet.javawebserver.util;

import java.util.HashMap;

public abstract class ConfigFormat {
	public ConfigFormat() {
		
	}
	
	public abstract void format(HashMap<String, Object> map);
}
