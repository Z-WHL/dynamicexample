package com.windhoverlabs.ide.config;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.windhoverlabs.ide.config.ContextManager;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ContextManager.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.SWT_PATH, "/home/vagrant/development/eclipse/plugins/org.eclipse.swt.gtk.linux.x86_64_3.106.3.v20180329-0507.jar");
		store.setDefault(PreferenceConstants.CUSTOM_CLASS_PATH, "/home/vagrant/development/airliner/apps/sch/classes.jar");

	}
}
