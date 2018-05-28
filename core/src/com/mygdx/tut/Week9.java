package com.mygdx.tut;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Week9 extends ApplicationAdapter {
	SpriteBatch batch;
	ParticleSystem Particles = new ParticleSystem();

	boolean wasTouchted = false;

	@Override
	public void create () {

        Particles.init();
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        float deltaTime = Gdx.graphics.getDeltaTime();

        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        if(Gdx.input.isTouched() && !wasTouchted){
            int i = Particles.spawn(ParticleSystem.Type.SMOKE);
            Particles.position[i].set(x, y);
            i = Particles.spawn(ParticleSystem.Type.SMOKE);
            Particles.position[i].set(x, y);
            i = Particles.spawn(ParticleSystem.Type.SMOKE);
            Particles.position[i].set(x, y);
            i = Particles.spawn(ParticleSystem.Type.EXPLOSION);
            Particles.position[i].set(x, y);
        }
        Particles.update(Gdx.graphics.getDeltaTime());
        wasTouchted = Gdx.input.isTouched();

        Particles.render(batch);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
        Particles.dispose();
	}
}
