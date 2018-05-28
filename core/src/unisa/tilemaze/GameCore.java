package unisa.tilemaze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**UniSA Tiled-LibGDX implementation prac.
 *
 * TiledMap implementation sourced from
 * http://www.gamefromscratch.com/post/2014/04/16/LibGDX-Tutorial-11-Tiled-Maps-Part-1-Simple-Orthogonal-Maps.aspx
 * follow link for more details.*/
 public class GameCore extends ApplicationAdapter {

    public enum GameState { PLAYING, COMPLETE }

    public static final float MOVEMENT_SPEED = 200.0f;
    public static final float GOAL_BOB_HEIGHT = 5.0f;
    public static final float SURFACE_CLIP_EPSILON = 0.01f;

    GameState gameState = GameState.PLAYING;

    //Map and rendering
    SpriteBatch spriteBatch;
    SpriteBatch uiBatch; //Second SpriteBatch without camera transforms, for drawing UI
    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;
    OrthographicCamera camera;

    //Game clock
    long lastTime;
    float elapsedTime;

    //Player Character
    Texture playerTexture;
    Sprite playerSprite;
    Vector2 playerDelta;
    Rectangle playerDeltaRectangle;

    //Goal
    Texture goalTexture;
    Sprite goalSprite;
    Vector2 goalPosition;
    float goalBobSine;

    //Storage class for collision
    Rectangle tileRectangle;

    //UI textures
    Texture buttonSquareTexture;
    Texture buttonSquareDownTexture;
    Texture buttonLongTexture;
    Texture buttonLongDownTexture;

    //UI Buttons
    Button moveLeftButton;
    Button moveRightButton;
    Button moveDownButton;
    Button moveUpButton;
    Button restartButton;
    //Just use this to only restart when the restart button is released instead of immediately as it's pressed
    boolean restartActive;

    /**Setup when the game is opened. It is important to set default values in here instead of
     * during instantiation, as those values aren't always reset after closing and reopening the
     * application. */
	@Override
	public void create () {

        //LibGDX Settings
        Gdx.app.setLogLevel(Application.LOG_DEBUG); //Allows sending messages to Logcat

        //Rendering
        spriteBatch = new SpriteBatch();
        uiBatch = new SpriteBatch();
        tiledMap = new TmxMapLoader().load("SimpleMaze.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        //Camera
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w/h * 250, 250);

        //Textures
        playerTexture = new Texture("player.png");
        goalTexture = new Texture("goal.png");
        buttonSquareTexture = new Texture("buttonSquare_blue.png");
        buttonSquareDownTexture = new Texture("buttonSquare_beige_pressed.png");
        buttonLongTexture = new Texture("buttonLong_blue.png");
        buttonLongDownTexture = new Texture("buttonLong_beige_pressed.png");

        //Player
        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(24, 24);
        playerDelta = new Vector2();
        playerDeltaRectangle = new Rectangle(0, 0, playerSprite.getWidth(), playerSprite.getHeight());

        //Goal
        goalSprite = new Sprite(goalTexture);
        goalPosition = new Vector2(0,0);
        goalBobSine = 0.0f;

        //Collision
        tileRectangle = new Rectangle();
        MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
        TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;
        tileRectangle.width = tileLayer.getTileWidth();
        tileRectangle.height = tileLayer.getTileHeight();

        //Buttons
        float buttonSize = h * 0.2f;
        moveLeftButton = new Button(0.0f, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
        moveRightButton = new Button(buttonSize*2, buttonSize, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
        moveDownButton = new Button(buttonSize, 0.0f, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
        moveUpButton = new Button(buttonSize, buttonSize*2, buttonSize, buttonSize, buttonSquareTexture, buttonSquareDownTexture);
        restartButton = new Button(w/2 - buttonSize*2, h * 0.2f, buttonSize*4, buttonSize, buttonLongTexture, buttonLongDownTexture);

        newGame();
	}

    private void newGame() {
        gameState = GameState.PLAYING;

        //Translate camera to center of screen
        camera.position.x = 16;
        camera.position.y = 16;

        lastTime = System.currentTimeMillis();
        elapsedTime = 0.0f;

        MapLayer objectLayer = tiledMap.getLayers().get("Objects");

        //Player start location, loaded from the tilemaze's object layer.
        RectangleMapObject playerObject = (RectangleMapObject) objectLayer.getObjects().get("Player");
        playerSprite.setCenter(playerObject.getRectangle().x, playerObject.getRectangle().y);
        camera.translate(playerSprite.getX(), playerSprite.getY());

        //Goal Location
        RectangleMapObject goalObject = (RectangleMapObject) objectLayer.getObjects().get("Goal");
        goalPosition.x = goalObject.getRectangle().x - 16;
        goalPosition.y = goalObject.getRectangle().y - 16;

        restartActive = false;
    }

    /**Cleanup done after the game closes. */
    @Override
    public void dispose() {
        tiledMap.dispose();

        playerTexture.dispose();
        buttonSquareTexture.dispose();
        buttonSquareDownTexture.dispose();
        buttonLongTexture.dispose();
        buttonLongDownTexture.dispose();
    }

    /**Method for all game logic. This method is called at the start of GameCore.render() below. */
    private void update() {
        //Touch Input Info
        boolean checkTouch = Gdx.input.isTouched();
        int touchX = Gdx.input.getX();
        int touchY = Gdx.input.getY();

        //Update Game State based on input
        switch (gameState) {

            case PLAYING:
                //Poll user for input
                moveLeftButton.update(checkTouch, touchX, touchY);
                moveRightButton.update(checkTouch, touchX, touchY);
                moveDownButton.update(checkTouch, touchX, touchY);
                moveUpButton.update(checkTouch, touchX, touchY);

                int moveX = 0;
                int moveY = 0;
                if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT) || moveLeftButton.isDown) {
                    moveLeftButton.isDown = true;
                    moveX -= 1;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT) || moveRightButton.isDown) {
                    moveRightButton.isDown = true;
                    moveX += 1;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN) || moveDownButton.isDown) {
                    moveDownButton.isDown = true;
                    moveY -= 1;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP) || moveUpButton.isDown) {
                    moveUpButton.isDown = true;
                    moveY += 1;
                }

                //TODO Determine character movement distance
                playerDelta.x = moveX * MOVEMENT_SPEED * elapsedTime;
                playerDelta.y = moveY * MOVEMENT_SPEED * elapsedTime;

                //Check movement against grid
                if (playerDelta.len2() > 0) { //Don't do anything if we're not moving
                    //Retrieve Collision layer
                    MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
                    TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;

                    //TODO Determine bounds to check within
// Find top-right corner tile
                    int right = (int) Math.ceil(Math.max(
                            playerSprite.getX() + playerSprite.getWidth(),
                            playerSprite.getX() + playerSprite.getWidth() + playerDelta.x)
                    );
                    int top = (int) Math.ceil(Math.max(
                            playerSprite.getY() + playerSprite.getHeight(),
                            playerSprite.getY() + playerSprite.getHeight() + playerDelta.y)
                    );
// Find bottom-left corner tile
                    int left = (int) Math.floor(Math.min(
                            playerSprite.getX(),
                            playerSprite.getX() + playerDelta.x)
                    );
                    int bottom = (int) Math.floor(Math.min(
                            playerSprite.getY(),
                            playerSprite.getY() + playerDelta.y)
                    );
// Divide bounds by tile sizes to retrieve tile indices
                    right /= tileLayer.getTileWidth();
                    top /= tileLayer.getTileHeight();
                    left /= tileLayer.getTileWidth();
                    bottom /= tileLayer.getTileHeight();

                    //TODO Loop through selected tiles and correct by each axis
                    // EXTRA: Try counting down if moving left or down instead of couting up
                    for (int y = bottom; y <= top; y++) {
                        for (int x = left; x <= right; x++) {
                            TiledMapTileLayer.Cell targetCell = tileLayer.getCell(x, y);
// If the cell is empty, ignore it
                            if (targetCell == null)
                                continue;
                            tileRectangle.x = x * tileLayer.getTileWidth();
                            tileRectangle.y = y * tileLayer.getTileHeight();
                            playerDeltaRectangle.x = playerSprite.getX() + playerDelta.x;
                            playerDeltaRectangle.y = playerSprite.getY() + playerDelta.y;
// Check to see if the player’s destination collides with the tile
                            if (!tileRectangle.overlaps(playerDeltaRectangle))
                                continue;
                            // Only correct against one axis at a time to prevent getting caught on corners
                            if (Math.abs(playerDelta.x) > Math.abs(playerDelta.y)) {
// Only check in the direction the player is actually moving
// if the player isn’t moving in this axis, then there’s no need to
// check anything
                                if (playerDelta.x > 0) {
                                    float difference = (playerDeltaRectangle.x + playerDeltaRectangle.width)
                                            - tileRectangle.x;
                                    playerDelta.x -= difference + SURFACE_CLIP_EPSILON;
                                } else if (playerDelta.x < 0) {
                                    float difference = (tileRectangle.x + tileRectangle.width)
                                            - playerDeltaRectangle.x;
                                    playerDelta.x += difference + SURFACE_CLIP_EPSILON;
                                }
                            } else {
                                if (playerDelta.y > 0) {
                                    float difference = (playerDeltaRectangle.y + playerDeltaRectangle.height)
                                            - tileRectangle.y;
                                    playerDelta.y -= difference + SURFACE_CLIP_EPSILON;
                                } else if (playerDelta.y < 0) {
                                    float difference = (tileRectangle.y + tileRectangle.height)
                                            - playerDeltaRectangle.y;
                                    playerDelta.y += difference + SURFACE_CLIP_EPSILON;
                                }
                            }

                        }
                    }

                    //TODO Move player
                    playerSprite.translate(playerDelta.x, playerDelta.y);
                    camera.translate(playerDelta.x, playerDelta.y);
                }

                //TODO Check if player has met the winning condition
                if (playerSprite.getBoundingRectangle().overlaps(
                        goalSprite.getBoundingRectangle())) {
                        //Player has won!
                    gameState = GameState.COMPLETE;
                }
                break;

            case COMPLETE:
                //Poll for input
                restartButton.update(checkTouch, touchX, touchY);

                if (Gdx.input.isKeyPressed(Input.Keys.DPAD_CENTER) || restartButton.isDown) {
                    restartButton.isDown = true;
                    restartActive = true;
                } else if (restartActive) {
                    newGame();
                }
                break;
        }

        goalBobSine += elapsedTime;
        goalBobSine %= Math.PI;
        goalSprite.setPosition(goalPosition.x, goalPosition.y + (GOAL_BOB_HEIGHT / 2.0f) -
                (GOAL_BOB_HEIGHT * (float) Math.sin(goalBobSine)));
    }

    /**Main game loop, all logic and rendering should be called from in here. */
	@Override
	public void render () {
        //Game World Update ------------------------------------------------------------------------

        //TODO Update game clock first
        long currentTime = System.currentTimeMillis();
        elapsedTime = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        update();

        //Rendering --------------------------------------------------------------------------------

        //Clear the screen every frame before drawing.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); //Allows transparent sprites/tiles
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        //Apply camera to spritebatch and draw player
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        goalSprite.draw(spriteBatch);
        playerSprite.draw(spriteBatch);
        spriteBatch.end();

        //Draw UI
        uiBatch.begin();
        switch(gameState) {
            //if gameState is Running: Draw Controls
            case PLAYING:
                moveLeftButton.draw(uiBatch);
                moveRightButton.draw(uiBatch);
                moveDownButton.draw(uiBatch);
                moveUpButton.draw(uiBatch);
                break;
            //if gameState is Complete: Draw Restart button
            case COMPLETE:
                restartButton.draw(uiBatch);
                break;
        }
        uiBatch.end();

	}
}
