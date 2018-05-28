package unisa.tilemaze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/**UniSA Tiled-LibGDX implementation prac.
 *
 * TiledMap implementation sourced from
 * http://www.gamefromscratch.com/post/2014/04/16/LibGDX-Tutorial-11-Tiled-Maps-Part-1-Simple-Orthogonal-Maps.aspx
 * follow link for more details.*/
 public class GameCore extends ApplicationAdapter {

    public enum GameState { PLAYING, COMPLETE };

    public static final float MOVEMENT_COOLDOWN_TIME = 0.3f;

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
    Texture characterTexture;
    int characterX;
    int characterY;
    float movementCooldown;

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

        //Rendering
        spriteBatch = new SpriteBatch();
        uiBatch = new SpriteBatch();

        //TODO Initiate the TiledMap and its renderer
        tiledMap = new TmxMapLoader().load("SimpleMaze.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        //Camera
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w / 2, h / 2);

        //Textures
        characterTexture = new Texture("character.png");
        buttonSquareTexture = new Texture("buttonSquare_blue.png");
        buttonSquareDownTexture = new Texture("buttonSquare_beige_pressed.png");
        buttonLongTexture = new Texture("buttonLong_blue.png");
        buttonLongDownTexture = new Texture("buttonLong_beige_pressed.png");

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
        camera.position.x = 16; //The 16 is half the size of a tile
        camera.position.y = 16;

        lastTime = System.currentTimeMillis();
        elapsedTime = 0.0f;

        //Player start location, you can have this stored in the tilemaze using an object layer.
        characterX = 1;
        characterY = 18;
        movementCooldown = 0.0f;

        camera.translate(characterX * 32, characterY * 32);
        restartActive = false;
    }

    /**Cleanup done after the game closes. */
    @Override
    public void dispose() {
        //TODO Dispose of the TileMap during application disposal
        tiledMap.dispose();

        characterTexture.dispose();
        buttonSquareTexture.dispose();
        buttonSquareDownTexture.dispose();
        buttonLongTexture.dispose();
        buttonLongDownTexture.dispose();
    }

    /**Main game loop, all logic and rendering should be called from in here. */
	@Override
	public void render () {
        //Update game clock first
        long currentTime = System.currentTimeMillis();
        //Divide by a thousand to convert from milliseconds to seconds
        elapsedTime = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        //Update the Game State
        update();

        //Clear the screen before drawing.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); //Allows transparent sprites/tiles
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        //TODO Render Map Here
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        //Draw Character
        //Apply the camera's transform to the SpriteBatch so the character is drawn in the correct
        //position on screen.
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(characterTexture, characterX * 32, characterY * 32, 32, 32);
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

    /**Method for all game logic. This method is called at the start of GameCore.render() before
     * any actual drawing is done. */
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
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || moveLeftButton.isDown) {
                    moveLeftButton.isDown = true;
                    moveX -= 1;
                } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || moveRightButton.isDown) {
                    moveRightButton.isDown = true;
                    moveX += 1;
                } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || moveDownButton.isDown) {
                    moveDownButton.isDown = true;
                    moveY -= 1;
                } else if (Gdx.input.isKeyPressed(Input.Keys.UP) || moveUpButton.isDown) {
                    moveUpButton.isDown = true;
                    moveY += 1;
                }

                //Movement update
                if (movementCooldown <= 0.0f) { //Don't move every frame

                    //TODO Retrieve Collision layer
                    MapLayer collisionLayer = tiledMap.getLayers().get("Collision");
                    TiledMapTileLayer tileLayer = (TiledMapTileLayer) collisionLayer;

                    //Don't do anything if we're not moving
                    if ((moveX != 0 || moveY != 0)
                            //TODO Also check map bounds to prevent exceptions when accessing map cells
                            && moveX + characterX >= 0 && moveX + characterX < tileLayer.getWidth()
                            && moveY + characterY >= 0 && moveY + characterY < tileLayer.getHeight()
                            ) {

                        //TODO Retrieve Target Tile
                        TiledMapTileLayer.Cell targetCell =
                                tileLayer.getCell(characterX + moveX, characterY + moveY);

                        //TODO Move only if the target cell is empty
                        if (targetCell == null) {
                            camera.translate(moveX * 32, moveY * 32);
                            characterX += moveX;
                            characterY += moveY;
                            movementCooldown = MOVEMENT_COOLDOWN_TIME; //Restrict movement for a moment
                        }
                    }
                }

                //Check if player has met the winning condition
                if (characterX == 18 && characterY == 1) {
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

        if (movementCooldown > 0.0f)
            movementCooldown -= elapsedTime;
    }
}
