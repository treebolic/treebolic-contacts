package org.treebolic.contacts;

public class ProviderData
{
	public static String[] getProviderClasses()
	{
		return new String[]{"treebolic.provider.contacts.Provider"};
	}

	public static String getMimetype()
	{
		return null;
	}

	public static String getExtensions()
	{
		return null;
	}

	public static String getUrlScheme()
	{
		return "contact:";
	}

	public static String getStyle()
	{
		return null;
	}
}
