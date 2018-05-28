package com.mygdx.tut;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by jackh on 28/05/2018.
 */

public class ParticleSystem {
    public static final int MAX_PARTICLES = 128;
    public static final float EXPLOSION_LIFETIME = 0.5f;
    public static final float  SMOKE_LIFETIME = 3.0f;

    public enum Type {NONE, EXPLOSION, SMOKE};

    public Texture spritesheet;
    public TextureRegion[] explosionFrames = new TextureRegion[10];
    public TextureRegion[] smokeFrames = new TextureRegion[6];

    Type[] type = new Type[MAX_PARTICLES];
    Vector2[] position = new Vector2[MAX_PARTICLES];
    Vector2[] velocity = new Vector2[MAX_PARTICLES];
    float[] lifetime = new float[MAX_PARTICLES];

    public void init(){
        spritesheet = new Texture(Gdx.files.internal("explosion.png"));

        explosionFrames[0] = new TextureRegion(spritesheet, 2, 2, 87, 87);
        explosionFrames[1] = new TextureRegion(spritesheet, 94, 2, 87, 87);
        explosionFrames[2] = new TextureRegion(spritesheet, 186, 2, 87, 87);
        explosionFrames[3] = new TextureRegion(spritesheet, 278, 2, 87, 87);
        explosionFrames[4] = new TextureRegion(spritesheet, 370, 2, 87, 87);
        explosionFrames[5] = new TextureRegion(spritesheet, 2, 94, 87, 87);
        explosionFrames[6] = new TextureRegion(spritesheet, 94, 94, 87, 87);
        explosionFrames[7] = new TextureRegion(spritesheet, 186, 94, 87, 87);
        explosionFrames[8] = new TextureRegion(spritesheet, 278, 94, 87, 87);
        explosionFrames[9] = new TextureRegion(spritesheet, 370, 94, 87, 87);

        smokeFrames[0] = new TextureRegion(spritesheet, 2, 306, 45,45);
        smokeFrames[1] = new TextureRegion(spritesheet, 52, 306, 45,45);
        smokeFrames[2] = new TextureRegion(spritesheet, 102, 306, 45,45);
        smokeFrames[3] = new TextureRegion(spritesheet, 152, 306, 45,45);
        smokeFrames[4] = new TextureRegion(spritesheet, 202, 306, 45,45);
        smokeFrames[5] = new TextureRegion(spritesheet, 252, 306, 45,45);

        for(int i =0; i<MAX_PARTICLES; i++){
            type[i]=Type.NONE;
            position[i]= new Vector2(0,0);
            velocity[i]= new Vector2(0,0);
            lifetime[i]= 0;
        }
    }
    public void update(float deltaTime) {
        for(int i =0; i<MAX_PARTICLES; i++) {
            if(type[i]!=Type.NONE){
                System.out.println(type[i] +" - "+lifetime[i]);
                if(lifetime[i]<=0){
                    type[i]=Type.NONE;
                }else{
                    position[i].mulAdd(velocity[i], deltaTime);
                }
            }
        }
    }

    public void render (SpriteBatch SB) {
        for(int i =0; i<MAX_PARTICLES; i++) {
            if(type[i]!=Type.NONE && lifetime[i]>0) {
                if(type[i]==Type.EXPLOSION) {
                    SB.begin();
                    SB.draw(explosionFrames[3], 1, 1);
                    SB.end();
                }else{
                    SB.begin();
                    SB.draw(smokeFrames[3], 1, 1);
                    SB.end();
                }
            }
            }
    }
        public int spawn(Type t){
        if(t==null) return -1;

        int i = -1;

        for(int free = 0;free< MAX_PARTICLES; free++){
            if(type[free] == Type.NONE){
                i=free;
                break;
            }
        }

        if(i<0) return -1;

        type[i] = t;
            position[i] = new Vector2(50,50);
            if(t==Type.SMOKE) {
                velocity[i] = new Vector2(MathUtils.random(-20f, 20f),MathUtils.random(-20f, 20f));
                lifetime[i] = SMOKE_LIFETIME;
            }else if(t==Type.EXPLOSION) {
                lifetime[i] = EXPLOSION_LIFETIME;
            }


        return i;
    }
    public void dispose () {
        spritesheet.dispose();
    }
}
