package com.windhoverlabs.ide.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DynamicClassLoader {

	private static ClassLoader classLoader;
	private static Class<?> initiator;
	
	// Function will check if the class is in the jars that have been loaded.
	public static boolean verifyClassExistence(String className, Composite parent) {	
		try {
			Class.forName(className, false, classLoader);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}	
	}
	
	// Initiates the class loader and loads the jar classes.
	public static boolean setUp(String jarPath, String classPath, Composite parent) {
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
