/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogicgames.superjumper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.badlogic.gdx.Gdx;
import com.badlogicgames.utils.Log;

public class Settings {
	static String TAG = "Settings";
	public static boolean soundEnabled = true;
	public final static int[] highscores = new int[] {100, 80, 50, 30, 10};
	//record the height.
	public final static int[] heightScores = new int[] {0, 0, 0, 0, 0};
	public final static String file = ".superjumper";
	static char mEndOfLine ='\n';

	public static void load () {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(Gdx.files.external(file).read()));
			soundEnabled = Boolean.parseBoolean(in.readLine());
			for (int i = 0; i < 5; i++) {
				highscores[i] = Integer.parseInt(in.readLine());
			}
			//read height scores from file.
			for (int i = 0; i < heightScores.length; i++) {
				heightScores[i] = Integer.parseInt(in.readLine());
			}
		} catch (Throwable e) {
			// :( It's ok we have defaults
			e.printStackTrace();
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
			}
		}
	}


	public static void save () {
		Log.d(TAG, " soundEnabled is "+ soundEnabled );
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(Gdx.files.external(file).write(false)));
			out.write(Boolean.toString(soundEnabled));
			out.write(mEndOfLine);
			for (int i = 0; i < 5; i++) {
				out.write(Integer.toString(highscores[i]));
				out.write(mEndOfLine);
			}
			for(int i =0; i < heightScores.length; i++){
				Log.d(TAG, "score " + i+ " is" + heightScores[i]);
				out.write(Integer.toString(heightScores[i]));
				out.write(mEndOfLine);
			}

		} catch (Throwable e) {
			Log.d(TAG, "exception e is:" + e);
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
			}
		}
	}

	public static void addScore (int score) {
		for (int i = 0; i < 5; i++) {
			if (highscores[i] < score) {
				for (int j = 4; j > i; j--)
					highscores[j] = highscores[j - 1];
				highscores[i] = score;
				break;
			}
		}
	}
	//update new height scores.
	public static void addHeightScore (int heightScore) {
		for (int i = 0; i < heightScores.length; i++) {
			if (heightScores[i] < heightScore) {
				for (int j = heightScores.length - 1; j > i; j--) {
					heightScores[j] = heightScores[j - 1];
				}
				heightScores[i] = heightScore;
				break;
			}
		}
	}
}
