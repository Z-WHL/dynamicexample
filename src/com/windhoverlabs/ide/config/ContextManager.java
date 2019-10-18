package com.windhoverlabs.ide.config;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * The activator class controls the plug-in life cycle
 */
public class ContextManager extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = "com.windhoverlabs.ide";
	// The shared instance
	public static ContextManager PLUGIN;
	private ResourceBundle _coreResourcesBundle;
	private boolean _isSuspended;
	
	{
		try {
			_coreResourcesBundle = ResourceBundle.getBundle("CorePluginResources");
		} catch (MissingResourceException e) {
			_coreResourcesBundle = null;
		}
	}
	
	/**
	 * The constructor
	 */
	public ContextManager() {
		PLUGIN = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		IPreferenceStore psc = ContextManager.getDefault().getPreferenceStore();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		PLUGIN = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ContextManager getDefault() {
		return PLUGIN;
	}
	
}
