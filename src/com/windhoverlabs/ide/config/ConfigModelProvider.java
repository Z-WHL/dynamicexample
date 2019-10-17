package com.windhoverlabs.ide.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ConfigModelProvider {
	
	private List<NamedObject> jsonElements;
	
	
	public ConfigModelProvider(JsonElement json, NamedObject nameObj) {
		jsonElements = new ArrayList<NamedObject>();
		for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonObject()) {
					JsonElement tempJe = entry.getValue();
					JsonObject tempJo = tempJe.getAsJsonObject();
					
					NamedObject toAdd = new NamedObject();
					toAdd.setName(entry.getKey());
					toAdd.setObject(tempJo);
					toAdd.setPath(nameObj.getPath().concat("."+entry.getKey()));
					jsonElements.add(toAdd);
			}
		}
	}
	
	public List<NamedObject> getJsons() {
		return jsonElements;
	}
}
