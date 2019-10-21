package com.windhoverlabs.ide.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DynamicClassLoader {

	private static ClassLoader classLoader;
	
	// Function will check if the class is in the jars that have been loaded.
	public static boolean verifyClassExistence(String className) {	
		try {
			Class.forName(className, false, classLoader);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}	
	}
	
	// Initiates the class loader and loads the jar classes.
	public static boolean setUp(String classPath, ArrayList<String> classPaths) {
		/*
		if (classPaths.size() > 0) {
			try {
				// Create an array of the custom classes.
				Object[] classPathsObjects = classPaths.toArray();
				String[] classPathStrings = new String[classPathsObjects.length];
				System.out.println(classPathStrings[0]);
				for (int i = 0; i < classPathsObjects.length; i++) {
					classPathStrings[i] = (String) classPathsObjects[i];
					System.out.println(classPathStrings[i]);
				}
				
				// We are assured there is at least one class.
				URL jarFile = new URL("file://"+classPathStrings[0]);
			
				ClassLoader classLoader = URLClassLoader.newInstance(new URL[] {jarFile}, parent.getClass().getClassLoader());
				
				String currentSelection = "com.windhoverlabs.airliner.apps.sch.Initiator";
			
				Class<?> initiator = Class.forName(currentSelection, true, classLoader);
				
				
				Object[] arguments = new Object[1];
				String[] argumentString = new String[classPathsObjects.length + 1];
				argumentString[0] = jarPath;
				
				for (int i = 0; i < classPathsObjects.length; i++) {
					argumentString[i +  1] = classPathStrings[i];
				}
				
				arguments[0] = argumentString;
				
				Method mainMethod = initiator.getMethod("main", String[].class);
				mainMethod.invoke(null,  arguments);
				return true;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();

				return false;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();

				return false;
			} catch (MalformedURLException e) {
				e.printStackTrace();

				return false;
			}
		} else {
			// Class paths were empty. Not necessary to load classes!
			return false;
		}
		
		
		try {
			URL jarFile = new URL("file://"+classPath);
			classLoader = URLClassLoader.newInstance(new URL[] {jarFile}, parent.getClass().getClassLoader());
			String currentSelection = "com.windhoverlabs.airliner.apps.sch.Initiator";
			initiator = Class.forName(currentSelection, true, classLoader);
			
			Object[] arguments = new Object[1];
			arguments[0] = new String[] { jarPath, classPath };
			Method mainMethod = initiator.getMethod("main", String[].class);
			mainMethod.invoke(null,  arguments);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		} catch (MalformedURLException e) {
			return false;
		}*/
		
		try {
			URL jarFile = new URL("file://"+classPath);

			classLoader = URLClassLoader.newInstance(new URL[] {jarFile}, Composite.class.getClassLoader());

			Class<?> urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);

			method.invoke(classLoader, new Object[] { jarFile });
			
			return true;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return false;
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	// Retrieves the Class from the loaded jars. The function that calls this should first check if it contains the class.
	public static Composite createCustomClass(NamedObject currentNamedObject, String className, Composite parent){
		Composite returned = null;
		try {
			
			Class<?> theComposite  = Class.forName(className, true, classLoader);
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


}
