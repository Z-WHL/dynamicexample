package com.windhoverlabs.ide.config;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DynamicClassLoader extends URLClassLoader {
	public DynamicClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// Check if we have already loaded the class from the jar
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass == null) {
			try {
				loadedClass = findClass(name);
			} catch (ClassNotFoundException e) {
				loadedClass = super.loadClass(name, resolve);
			}
		}
		
		if (resolve) {
			resolveClass(loadedClass);
		}
		return loadedClass;
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		List<URL> allRes = new LinkedList<>();
		Enumeration<URL> thisRes = findResources(name);
		if (thisRes != null) {
			while (thisRes.hasMoreElements()) {
				allRes.add(thisRes.nextElement());
			}
		}
		
		Enumeration<URL> parentRes = super.findResources(name);
		if (parentRes != null) {
			while (parentRes.hasMoreElements()) {
				allRes.add(parentRes.nextElement());
			}
		}
		
		return new Enumeration<URL>() {
			Iterator<URL> it = allRes.iterator();
			@Override
			
			public boolean hasMoreElements() {
				return it.hasNext();
			}
			
			@Override
			public URL nextElement() {
				return it.next();
			}
		};
	}
	
	@Override
	public URL getResource(String name) {
		URL res = findResource(name);
		if (res == null) {
			res = super.getResource(name);
		}
		return res;
	}
	
}
