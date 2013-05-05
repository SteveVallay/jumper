package com.badlogicgames.superjumper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogicgames.utils.Log;

public class LevelScreen implements Screen{
	Game game;
	OrthographicCamera guiCam;
	SpriteBatch batcher;
	Stage stage;
	
	public static final int SIZE = 84;
	public static final int PADDING = 300;
	public static final int LEVEL = 5;
	public static final String TAG = "LevelScreen";
	
	TextureRegion upRegion[] = new TextureRegion[LEVEL];
	TextureRegion downRegion[] = new TextureRegion[LEVEL];
	TextButton buttonts[] = new TextButton[LEVEL];

	public LevelScreen(final Game game){
		this.game = game;	
		guiCam = new OrthographicCamera(320, 480);
		guiCam.position.set(320 / 2, 480 / 2, 0);
		batcher = new SpriteBatch();
		 stage = new Stage(320,480, false);
       Gdx.input.setInputProcessor(stage);
       Table table = new Table();
       //table.setFillParent(true);
       table.setLayoutEnabled(true);
       table.setPosition(240, 0);
       table.addAction(Actions.moveTo(240, 400, 0.4f));
       table.top().left().pad(PADDING,PADDING,PADDING,PADDING);

       stage.addActor(table);	

		upRegion[0] = Assets.btnUpRegion1;
		downRegion[0] = Assets.btnDownRegion1;
		upRegion[1] = Assets.btnUpRegion2;
		downRegion[1] = Assets.btnDownRegion2;
		upRegion[2] = Assets.btnUpRegion3;
		downRegion[2] = Assets.btnDownRegion3;
		upRegion[3] = Assets.btnUpRegion4;
		downRegion[3] = Assets.btnDownRegion4;
		upRegion[4] = Assets.btnUpRegion5;
		downRegion[4] = Assets.btnDownRegion5;
		BitmapFont buttonFont = Assets.font;

		for(int i= LEVEL -1  ;i >= 0 ;i--){
			TextButtonStyle style = new TextButtonStyle();
			style.up = new TextureRegionDrawable(upRegion[i]);
			style.down = new TextureRegionDrawable(downRegion[i]);
			style.font = Assets.font;
			buttonts[i] = new TextButton("", style);
			final int a = i;
			buttonts[i].addListener(new ActorGestureListener(){
				  @Override
				    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					  super.touchUp(event, x, y, pointer, button);
					  game.setScreen(new GameScreen(game, a+1));
				    }
			});
			table.add(buttonts[i]).size(SIZE,SIZE).pad(50, 50, 50, 50).expand();
			table.row();
		}

	}

	@Override
	public void render (float delta) {
		// TODO Auto-generated method stub
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		guiCam.update();
		batcher.setProjectionMatrix(guiCam.combined);

		batcher.disableBlending();
		batcher.begin();
		batcher.draw(Assets.levelRegion, 0, 0, 320, 480);
		batcher.end();

//		batcher.enableBlending();
//		batcher.begin();
//		//batcher.draw(Assets.btnTexture, 160,240);
//		batcher.draw(Assets.btnUpRegion, 160, 240);
//		batcher.end();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		Table.drawDebug(stage);
	}

	@Override
	public void resize (int width, int height) {
		// TODO Auto-generated method stub
		 stage.setViewport(width, height, true);
	}

	@Override
	public void show () {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide () {
		// TODO Auto-generated method stub
		Log.d(TAG, "hide");
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause () {
		// TODO Auto-generated method stub
		Log.d(TAG, "pause");
	}

	@Override
	public void resume () {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose () {
		// TODO Auto-generated method stub
		Log.d(TAG, "dispose");
		 stage.dispose();
	}

}
