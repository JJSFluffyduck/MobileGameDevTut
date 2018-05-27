package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class MyGdxGame implements ApplicationListener {

	Texture walkSheet;      // #4
	SpriteBatch spriteBatch;        // #6
    TextureRegion[] walkFrames;

    Animation walkAnimation;
    TextureRegion currentFrame;
    int frameIndex;
    float stateTime;

    private static final int FRAME_COLS = 6;
	private static final int FRAME_ROWS = 5;

	@Override
	public void create() {

		walkSheet = new Texture(Gdx.files.internal("animation_sheet.png")); // #9

        TextureRegion[][] temp = TextureRegion.split(walkSheet,
                walkSheet.getWidth() / FRAME_COLS,
                walkSheet.getHeight() / FRAME_ROWS);
        walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                walkFrames[index++] = temp[i][j];
            }
        }

        spriteBatch = new SpriteBatch();                // #12

        walkAnimation = new Animation(0.033f, walkFrames);
        stateTime = 0.0f;


    }

	@Override
	public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = (TextureRegion) walkAnimation.getKeyFrame(stateTime, true);
        frameIndex = walkAnimation.getKeyFrameIndex(stateTime);
        Gdx.app.log("current time",Float.toString(stateTime));

        Gdx.app.log("current frame index",Integer.toString(frameIndex));
        spriteBatch.begin();
        spriteBatch.draw(currentFrame,1,1);
        spriteBatch.end();

	}

	@Override
	public void dispose() {
		// dispose of all the native resources

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}