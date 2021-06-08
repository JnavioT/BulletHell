package com.example.bullethell;

import android.app.Activity;
import android.view.Window;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class BulletHellActivity extends Activity {

    private BulletHellGame mBHGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Conseguir la resolución de la pantalla
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        // Llamar al constructor del juego BulletHellGame
        mBHGame = new BulletHellGame(this, size.x,size.y);
        setContentView(mBHGame);

    }

    @Override
    // Comienza el hilo principal del juego cuando es lanzado
    protected void onResume() {
        super.onResume();
        mBHGame.resume();
    }
    @Override
    // Detiene el hilo del juego cuando el jugador se retira
    protected void onPause() {
        super.onPause();
        mBHGame.pause();
    }
}