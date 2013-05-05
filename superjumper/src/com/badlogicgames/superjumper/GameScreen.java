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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogicgames.superjumper.World.WorldListener;
import com.badlogicgames.utils.Log;

public class GameScreen implements Screen {
	static final int GAME_READY = 0;
	static final int GAME_RUNNING = 1;
	static final int GAME_PAUSED = 2;
	static final int GAME_LEVEL_END = 3;
	static final int GAME_OVER = 4;
	static final int LEVEL_INIT = 1;
	static final int LEVEL_MAX = 5;
	static String TAG = "GameScreen";

	Game game;

	int state;
	OrthographicCamera guiCam;
	Vector3 touchPoint;
	SpriteBatch batcher;
	World world;
	WorldListener worldListener;
	WorldRenderer renderer;
	Rectangle pauseBounds;
	Rectangle resumeBounds;
	Rectangle quitBounds;
	int lastScore;
	int lastHeightScore;
	String scoreString;
	String heightScoreString;
	int mLevel = LEVEL_INIT;

	public GameScreen (Game game,int level) {
		this.game = game;
		Log.d(TAG, "game screen: level is:"+level);
		state = GAME_READY;
		guiCam = new OrthographicCamera(320, 480);
		guiCam.position.set(320 / 2, 480 / 2, 0);
		touchPoint = new Vector3();
		batcher = new SpriteBatch();
		worldListener = new WorldListener() {
			@Override
			public void jump () {
				Assets.playSound(Assets.jumpSound);
			}

			@Override
			public void highJump () {
				Assets.playSound(Assets.highJumpSound);
			}

			@Override
			public void hit () {
				Assets.playSound(Assets.hitSound);
			}

			@Override
			public void coin () {
				Assets.playSound(Assets.coinSound);
			}
		};
		generageWorld(level);
		pauseBounds = new Rectangle(320 - 64, 480 - 64, 64, 64);
		resumeBounds = new Rectangle(160 - 96, 240, 192, 36);
		quitBounds = new Rectangle(160 - 96, 240 - 36, 192, 36);
	}

	protected void generageWorld(int level){
		Log.d(TAG, "generate :"+level);
		mLevel = level;
		world = new World(worldListener);
		world.generateLevel(mLevel);
		renderer = new WorldRenderer(batcher, world);
		lastScore = 0;
		lastHeightScore = 0;
		scoreString = "SCORE: 0";
		heightScoreString = "SCORE: 0";
	}

	public void update (float deltaTime) {
		if (deltaTime > 0.1f) deltaTime = 0.1f;

		switch (state) {
		case GAME_READY:
			updateReady();
			break;
		case GAME_RUNNING:
			updateRunning(deltaTime);
			break;
		case GAME_PAUSED:
			updatePaused();
			break;
		case GAME_LEVEL_END:
			updateLevelEnd();
			break;
		case GAME_OVER:
			updateGameOver();
			break;
		}
	}

	private void updateReady () {
		if (Gdx.input.justTouched()) {
			state = GAME_RUNNING;
		}
	}

	private void updateRunning (float deltaTime) {
		if (Gdx.input.justTouched()) {
			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

			if (OverlapTester.pointInRectangle(pauseBounds, touchPoint.x, touchPoint.y)) {
				Assets.playSound(Assets.clickSound);
				state = GAME_PAUSED;
				return;
			}
		}
		
		ApplicationType appType = Gdx.app.getType();
		
		// should work also with Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)
		if (appType == ApplicationType.Android || appType == ApplicationType.iOS) {
			world.update(deltaTime, Gdx.input.getAccelerometerX());
		} else {
			float accel = 0;
			if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)) accel = 5f;
			if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)) accel = -5f;
			world.update(deltaTime, accel);
		}
		if (world.score != lastScore) {
			lastScore = world.score;
			scoreString = "SCORE: " + lastScore;
		}
		if (World.convertHeightFormat(world.heightSoFar) != lastHeightScore){
			lastHeightScore = World.convertHeightFormat(world.heightSoFar);
			heightScoreString = "SCORE: " + lastHeightScore;
		}
		if (world.state == World.WORLD_STATE_NEXT_LEVEL) {
			state = GAME_LEVEL_END;
		}
		if (world.state == World.WORLD_STATE_GAME_OVER) {
			state = GAME_OVER;
			//[draw height instead of coin scores][start]
			//do not show score , show height.
			if (lastScore >= Settings.highscores[4])
				scoreString = "NEW HIGHSCORE: " + lastScore;
			else
				scoreString = "SCORE: " + lastScore;
			if (lastHeightScore >= Settings.heightScores[Settings.heightScores.length - 1]) {
				heightScoreString = "NEW HIGH: " + lastHeightScore;
			} else {
				scoreString = "SCORE: " + lastHeightScore;
			}
			Settings.addHeightScore(lastHeightScore);
			//[draw height instead of coin scores][end]
			Settings.save();

		}
	}

	private void updatePaused () {
		if (Gdx.input.justTouched()) {
			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

			if (OverlapTester.pointInRectangle(resumeBounds, touchPoint.x, touchPoint.y)) {
				Assets.playSound(Assets.clickSound);
				state = GAME_RUNNING;
				return;
			}

			if (OverlapTester.pointInRectangle(quitBounds, touchPoint.x, touchPoint.y)) {
				Assets.playSound(Assets.clickSound);
				game.setScreen(new MainMenuScreen(game));
				return;
			}
		}
	}

	private void updateLevelEnd () {
		Log.d(TAG, "update level end:" + mLevel);
		if (Gdx.input.justTouched()) {
			if(mLevel < LEVEL_MAX){
				mLevel ++;
			   generageWorld(mLevel);
				state = GAME_READY;
				Log.d(TAG, "update level in:" + mLevel);
			}else{
				Log.d(TAG, "all level end...");
			}

		}
	}

	private void updateGameOver () {
		if (Gdx.input.justTouched()) {
			//game.setScreen(new MainMenuScreen(game));
			game.setScreen(new GameScreen(game, mLevel));
		}
	}

	public void draw (float deltaTime) {
		GLCommon gl = Gdx.gl;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		renderer.render();

		guiCam.update();
		batcher.setProjectionMatrix(guiCam.combined);
		batcher.enableBlending();
		batcher.begin();
		switch (state) {
		case GAME_READY:
			presentReady();
			break;
		case GAME_RUNNING:
			presentRunning();
			break;
		case GAME_PAUSED:
			presentPaused();
			break;
		case GAME_LEVEL_END:
			presentLevelEnd();
			break;
		case GAME_OVER:
			presentGameOver();
			break;
		}
		batcher.end();
	}

	private void presentReady () {
		batcher.draw(Assets.ready, 160 - 192 / 2, 240 - 32 / 2, 192, 32);
		String levelTextString = "Level:" + mLevel;
		float levelWidth = Assets.font.getBounds(levelTextString).width;
		Assets.font.draw(batcher, levelTextString, 160 - levelWidth/2,280);
	}

	private void presentRunning () {
		batcher.draw(Assets.pause, 320 - 64, 480 - 64, 64, 64);
		//[draw height instead of coin scores][start]
		//Assets.font.draw(batcher, scoreString, 16, 480);
		Assets.font.draw(batcher, heightScoreString, 16, 480 - 20);
		//[draw height instead of coin scores][end]
		Texture region;
		//batcher.draw(region, x, y)
		String levelTextString = "Level:" + mLevel;
		Assets.font.draw(batcher, levelTextString, 16, 480-40);

	}

	private void presentPaused () {
		batcher.draw(Assets.pauseMenu, 160 - 192 / 2, 240 - 96 / 2, 192, 96);
		//[draw height instead of coin scores][start]
		//Assets.font.draw(batcher, scoreString, 16, 480 - 20);
		Assets.font.draw(batcher, heightScoreString, 16, 480 - 40);
		//[draw height instead of coin scores][end]
	}

	private void presentLevelEnd () {
		String topText = "the princess is ...";
		String bottomText = "in another castle!";
		String levelTextString = "Level:" + mLevel;
		float topWidth = Assets.font.getBounds(topText).width;
		float bottomWidth = Assets.font.getBounds(bottomText).width;
		float levelWidth = Assets.font.getBounds(levelTextString).width;
		Assets.font.draw(batcher, topText, 160 - topWidth / 2, 480 - 40);
		Assets.font.draw(batcher, bottomText, 160 - bottomWidth / 2, 40);
		if(mLevel < LEVEL_MAX){
			Assets.font.draw(batcher, levelTextString, 160 - levelWidth / 2, 240);
		}
	}

	private void presentGameOver () {
		batcher.draw(Assets.gameOver, 160 - 160 / 2, 240 - 96 / 2, 160, 96);
		//[draw height instead of coin scores][start]
//		float scoreWidth = Assets.font.getBounds(scoreString).width;
//		Assets.font.draw(batcher, scoreString, 160 - scoreWidth / 2, 480 - 20);
		float scoreWidth = Assets.font.getBounds(heightScoreString).width;
		Assets.font.draw(batcher, heightScoreString, 160 - scoreWidth / 2, 480 - 20);
		//[draw height instead of coin scores][end]
	}

	@Override
	public void render (float delta) {
		update(delta);
		draw(delta);
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void show () {
	}

	@Override
	public void hide () {
	}

	@Override
	public void pause () {
		if (state == GAME_RUNNING) state = GAME_PAUSED;
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
