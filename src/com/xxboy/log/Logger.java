package com.xxboy.log;

import java.util.logging.Level;

public class Logger {
	public static void log(String msg) {
		java.util.logging.Logger.getAnonymousLogger().log(Level.INFO, msg);
	}

	public static void log(Throwable e) {
		java.util.logging.Logger.getAnonymousLogger().log(Level.INFO, e.getMessage(), e);
	}
}
