package net.sf.xsdutils.plugin;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class Messages {
	private static final String BUNDLE_NAME = "net.sf.xsdutils.plugin.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages() {
	}
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	public static String getString(String key, Object... args) {
		try {
			return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
