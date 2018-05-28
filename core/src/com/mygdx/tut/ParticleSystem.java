package com.mygdx.tut;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
}
