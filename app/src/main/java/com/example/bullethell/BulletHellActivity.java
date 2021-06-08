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
        // Get the screen resolution
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        // Call the constructor(initialize)
        // the BulletHellGame instance
        mBHGame = new BulletHellGame(this, size.x,size.y);
        setContentView(mBHGame);

    }

    @Override
    // Start the main game thread
    // when the game is launched
    protected void onResume() {
        super.onResume();
        mBHGame.resume();
    }
    @Override
    // Stop the thread when the player quits
    protected void onPause() {
        super.onPause();
        mBHGame.pause();
    }
}