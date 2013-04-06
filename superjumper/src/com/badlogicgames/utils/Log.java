package com.badlogicgames.utils;

import com.badlogic.gdx.Gdx;

public final class Log {
	
	public static void d(String tag, String message){
		Gdx.app.log(tag, message);
	}
	public static void e(String tag, String message){
		Gdx.app.error(tag, message);
	}
} 