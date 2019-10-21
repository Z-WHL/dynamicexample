package com.windhoverlabs.ide.config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DynamicClassLoader {

	private static URLClassLoader classLoader;
	//private static HashMap<String, URLClassLoader> classLoaders = new HashMap<>();
	
	/**
	 * Initiates the class loader and loads the jar classes.
	 * @param classPaths
	 * @return
	 */
	public static boolean setUpNew(ArrayList<String> classPaths) {
		if (classPaths.size() > 0) {
			try {
				// Iterate through our list of jar names to create an array of URL representation of our jars.
				URL[] jarFiles = new URL[classPaths.size()];
				for (int i = 0; i < classPaths.size(); i++) {
					String jarName = String.valueOf(classPaths.get(i));
					URL jarAsURL = new URL("file://" + jarName);
					jarFiles[i] = jarAsURL;
				} 
				// Using the base default Composite class loader, create a new instance with our array of URLs added.
				classLoader = URLClassLoader.newInstance(jarFiles, Composite.class.getClassLoader());
				return true;
			}catch (MalformedURLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Will update the classes. Two implementations. Delete the old monolithic classloader and recreate a new one.
	 * Retrieve the necessary ones, close them and remove them.
	 * @param updatedClassPaths
	 */
	public static void updateClasses(ArrayList<String> updatedClassPaths) {
		// Delete and recreate.
		try {
			classLoader.close();
			setUpNew(updatedClassPaths);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * Function will check if the class is in the jars that have been loaded.
	 * @param className
	 * @return
	 */
	public static boolean verifyClassExistence(String className) {	
		try {
			Class.forName(className, false, classLoader);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}	
		
		/*
		if (classLoaders.containsKey(className)) {
			return true;
		} else {
			return false;
		} */
	}
	
	/**
	 * Retrieves the Class from the loaded jars. The function that calls this should first check if it contains the class.
	 * @param currentNamedObject
	 * @param className
	 * @param parent
	 * @return
	 */
	public static Composite createCustomClass(NamedObject currentNamedObject, String className, Composite parent){
		Composite returned = null;
		try {
			ClassLoader classLoad = (ClassLoader) classLoader; 
			Class<?> theComposite  = Class.forName(className, true, classLoad);
			Class<? extends Composite> compositeClass = theComposite.asSubclass(Composite.class);
			Constructor<? extends Composite> constructor = compositeClass.getConstructor(Composite.class, int.class);
			returned = constructor.newInstance(parent, SWT.FILL);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} 
		return returned;
	}
	
	/*
	public static boolean setUp(ArrayList<String> classPaths) {
		if (classPaths.size() > 0) {
			try {
				// Iterate through our list of jar names to create a mapping of class names and class loaders.
				for (int i = 0; i < classPaths.size(); i++) {
					String jarName = String.valueOf(classPaths.get(i));
					URL jarAsURL = new URL("file://" + jarName);
					URLClassLoader oneClassLoader = URLClassLoader.newInstance(new URL[] { jarAsURL }, Composite.class.getClassLoader());
					classLoaders.put(jarName, oneClassLoader);
				} 
				return true;
			}catch (MalformedURLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	public static void updateHashSet(ArrayList<String> updatedClassPaths) {
		// Remove from map of class loaders.
		for (Entry<String, URLClassLoader> entry : classLoaders.entrySet()) {
			if (!updatedClassPaths.contains(entry.getKey())) {
				URLClassLoader oneClassLoader = entry.getValue();
				try {
					oneClassLoader.close();
					classLoaders.remove(entry.getKey());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		updatedClassPaths.forEach( (key) -> {
			if (!classLoaders.containsKey(key)) {
				URL jarAsURL;
				try {
					jarAsURL = new URL("file://" + key);
					URLClassLoader oneClassLoader = URLClassLoader.newInstance(new URL[] { jarAsURL }, Composite.class.getClassLoader());
					classLoaders.put(key, oneClassLoader);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
	} */

}
