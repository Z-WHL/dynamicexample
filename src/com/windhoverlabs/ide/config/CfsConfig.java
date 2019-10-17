package com.windhoverlabs.ide.config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeListener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class CfsConfig {
	
	private JsonElement base;
	private JsonElement local;
	private JsonElement combined;
	private String path;
	private boolean dirty;
	private IResourceChangeListener resourceChangeListener;
	private List<ICfsConfigChangeListener> listener = new ArrayList<ICfsConfigChangeListener>();
	
	public CfsConfig() {
		dirty = false;
	}
	
	
	public void addChangeListener(ICfsConfigChangeListener listener) {
		this.listener.add(listener);
	}
	
	public void removeChangeListener(ICfsConfigChangeListener listener) {
		this.listener.remove(listener);
	}
	
	public final boolean isDirty() {
		return dirty;
	}
	
	
	public void setBase(JsonElement config) {
		this.base = config;
	}
	
	
	public void createCombined() {
		this.combined = new JsonObject();
		
		JsonObjectsUtil.merge(this.base, this.combined);
		JsonObjectsUtil.merge(this.local, this.combined);
	}
	
	
	private final JsonElement getBase() {
		return this.base;
	}
	
	
	public void setLocal(JsonElement config) {
		this.local = config;
	}
	
	private JsonElement getLocal() {
		return this.local;
	}
	
	
	private String getPath() {
		return this.path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	
	
	public final JsonElement getJsonElementByPath(String path) {
		JsonElement jsonElement = getJsonElementByPath(path, JSONForm.COMBINED);
		
		return jsonElement;
	}
	
	
	private final JsonObject getJsonObjectByPath(String path, JSONForm which) {
		JsonElement tgtElement = null;
		
        switch(which) {
            case BASE:
                tgtElement = this.base;
                break;

            case LOCAL:
                tgtElement = this.local;
                break;

            default:
                tgtElement = this.combined;
                break;
        }
        
		String[] parts = path.split("\\.|\\[|\\]");
		JsonObject toSearchObject = tgtElement.getAsJsonObject();
		
		for (int i = 0; i < parts.length; i++) {
			if (i + 1 == parts.length) {
				if (toSearchObject.has(parts[i])) {
					toSearchObject = toSearchObject.get(parts[i]).getAsJsonObject();

					return toSearchObject;
				} else {
					return null;
				}
			}
			if (toSearchObject.has(parts[i])) {
				toSearchObject = toSearchObject.get(parts[i]).getAsJsonObject();
			} else {
				return null;
			}
		}
		return null;
	}
	
	
	private final NamedObject getNamedObjectByPath(String path, JSONForm which) {
		String[] parts = path.split("\\.|\\[|\\]");
		
		JsonObject jsonObj = getJsonObjectByPath(path, which);
		NamedObject namedObj = null; 
		
		if(jsonObj != null) {
			namedObj = new NamedObject();
			
			namedObj.setName(parts[parts.length - 1]);
			namedObj.setPath(path);
			namedObj.setObject(jsonObj);
			
			if(isOverridden(path)) {
				namedObj.setOverridden(true);
	    	} else {
	    		namedObj.setOverridden(false);
	    	}
		}

		return namedObj;
	}
	
	
	public final NamedObject getNamedObjectByPath(String path) {	
		NamedObject namedObj = getNamedObjectByPath(path, JSONForm.COMBINED);

		return namedObj;
	}
	
	
	public final JsonObject getJsonObjectByPath(String path) {
		JsonElement jsonElement = getJsonElementByPath(path);
		
		if(jsonElement.isJsonObject() == false) {
			return null;
		}
		
		return jsonElement.getAsJsonObject();
	}
	
	
	public boolean isOverridden(String path) {
		JsonElement result = getJsonElementByPath(path, JSONForm.LOCAL);
		
		if((result == null) || (result == JsonNull.INSTANCE)) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public String getPathOfParentElement(String path) {
		String[] parts = path.split("\\.|\\[|\\]");
		String parentPath = parts[0];
		
		
		for (int i = 1; i < parts.length-1; i++) {
			parentPath = parentPath + "." + parts[i];
		}
		
		return parentPath;
	}
	
	
	public String getElementNameByPath(String path) {
		String[] parts = path.split("\\.|\\[|\\]");
		String elementName = "";
		
		elementName = parts[parts.length-1];
		
		return elementName;
	}	
	
	public void UnoverrideByPath(String path) {
		String parentPath = getPathOfParentElement(path);
		String elementName = getElementNameByPath(path);

		JsonElement jsonLocalParentElement = getJsonElementByPath(parentPath, JSONForm.LOCAL);

	    JsonObject jsonLocalParentObject = (JsonObject) jsonLocalParentElement.getAsJsonObject();
	    
		jsonLocalParentObject.remove(elementName);
		
		jsonLocalParentElement = getJsonElementByPath(parentPath, JSONForm.LOCAL);
		
		int objSize = jsonLocalParentObject.size();
		
		if(objSize <= 0) {
			UnoverrideByPath(parentPath);
		}

		createCombined();
		makeDirty();
	}
	

	public boolean deleteCustomLocalAddition(String path) {
		System.out.println("deletecustomlocal " + path);
		if (getJsonObjectByPath(path, JSONForm.BASE) == null) {
			String parentPath = getPathOfParentElement(path);
			String elementName = getElementNameByPath(path);
			
			JsonElement parentElement = getJsonElementByPath(parentPath, JSONForm.LOCAL);
			JsonElement combinedElement = getJsonElementByPath(parentPath, JSONForm.COMBINED);
			
			JsonObject parentObject = parentElement.getAsJsonObject();
			JsonObject combinedObject = combinedElement.getAsJsonObject();
			
			parentObject.remove(elementName);
			combinedObject.remove(elementName);
			
			createCombined();

			makeDirty();
			if (parentObject.entrySet().size() == 0) {
				UnoverrideByPath(parentPath);
			}
			return true;
		} else {
			return false;
		}
	}
	
	// Java collections are known to throw Concurrent Modification if an entry makes an alteration. Changed to array.
    private void notifyListeners() {
    	Object[] obj = listener.toArray();
    	for (int i = 0; i < obj.length; i++) {
    		ICfsConfigChangeListener temp = (ICfsConfigChangeListener) obj[i];
    		temp.cfsConfigUpdated();
    	}
    }	
	
	private JsonElement getJsonElementByPath(String path, JSONForm which) {
		JsonElement tgtElement = null;
		
        switch(which) {
            case BASE:
                tgtElement = this.base;
                break;

            case LOCAL:
                tgtElement = this.local;
                break;

            default:
                tgtElement = this.combined;
                break;
        }
		
		String[] parts = path.split("\\.|\\[|\\]");
	    JsonElement result = tgtElement;

	    for (String key : parts) {

	        key = key.trim();
	        if (key.isEmpty())
	            continue;

	        if (result == null){
	            result = JsonNull.INSTANCE;
	            break;
	        }

	        if (result.isJsonObject()){
	            result = ((JsonObject)result).get(key);
	        }
	        else if (result.isJsonArray()){
	            int ix = Integer.valueOf(key) - 1;
	            result = ((JsonArray)result).get(ix);
	        }
	        else break;
	    }

	    return result;
	}
	

	private void makeDirty() {		
		dirty = true;

		notifyListeners();

	}
	
	
	private void makeClean() {
		dirty = false;

		notifyListeners();
	}
	
	
	private void setNamedObject(NamedObject namedObj, JSONForm which) {
		String[] parts = namedObj.getPath().split("\\.|\\[|\\]");
		int depth = parts.length;
		JsonElement localPointer;

		switch(which) {
        case BASE:
       	 localPointer = this.base;
            break;

        case LOCAL:
       	 localPointer = this.local;
            break;

        default:
       	 localPointer = this.combined;
            break;
		 }
		JsonObject localObject = localPointer.getAsJsonObject();
		
		for (int i = 0; i < depth; i++) {
			// You are currently at the selected element
			if (i + 1 == depth) {
				JsonElement toUpdate = (JsonElement) namedObj.getObject();
				localObject.add(namedObj.getName(), toUpdate);
				break;
			} 			
			// Let's check if the path exists and if it does update the crawl to use the json object.
			// If the path doesn't exist then create an empty object inside the current crawled object.
			if (localObject.has(parts[i])) {
				// Update the element to be crawled.
				localObject = localObject.get(parts[i]).getAsJsonObject(); 
			} else {
				localObject.add(parts[i], new JsonObject());
				localObject = localObject.get(parts[i]).getAsJsonObject();
			}			
		}
		
		switch(which) {
        case BASE:
       	 	this.base = localPointer;
            break;

        case LOCAL:
       	 	this.local = localPointer;
            break;

        default:
        	this.combined = localPointer;
            break;
		}
	}
	
	
	
	public void saveFile() {
		saveToFile(this.local);
		makeClean();
	}
	
	
	private void saveToFile(JsonElement json) {
		String toBeSaved = JsonObjectsUtil.beautifyJson(json.getAsJsonObject().toString());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			writer.write(toBeSaved);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void addObjectByPath(String objPath, JSONForm which) {
		String[] parts = objPath.split("\\.|\\[|\\]");
		JsonElement tgtElement = which.equals(JSONForm.LOCAL) ? this.local : this.base;
		JsonObject parentObject = tgtElement.getAsJsonObject();

        for (String elemName : parts) {
        	JsonElement curElement = parentObject.get(elemName);
        	
        	if(curElement == null) {
        		JsonObject newObject = new JsonObject();
        		parentObject.add(elemName, newObject);
        		parentObject = newObject;
  
        	} else if(curElement.isJsonObject() == false) {
        		/* TODO:  Add error handling */
        	} else {
            	parentObject = curElement.getAsJsonObject();
        	}
        }
	}

	public void setKeyValue(String objPath, String keyName, String value) {
		addObjectByPath(objPath, JSONForm.LOCAL);
		
		JsonObject localObject = getJsonObjectByPath(objPath, JSONForm.LOCAL);
		JsonObject combinedObject = getJsonObjectByPath(objPath, JSONForm.COMBINED);
		
		localObject.addProperty(keyName, value);
		combinedObject.addProperty(keyName, value);
		
		makeDirty();
	}
	

	public void addKey(String parentObjectPath, String key) {
		addObjectByPath(parentObjectPath, JSONForm.LOCAL);
		
		JsonObject localObject = getJsonObjectByPath(parentObjectPath, JSONForm.LOCAL);
		JsonObject combinedObject = getJsonObjectByPath(parentObjectPath, JSONForm.COMBINED);
		
		localObject.addProperty(key, " ");
		combinedObject.addProperty(key, " ");
		makeDirty();
	}
	
	public void update(NamedObject js) {
		// Update our in-memory object/representation
		setNamedObject(js, JSONForm.LOCAL);
		setNamedObject(js, JSONForm.COMBINED);
		// Now let's update the persistent representation. Can be file, database etc.
		
		makeDirty();
	}

	


}
