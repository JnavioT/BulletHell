package com.example.bullethell;

import android.graphics.RectF;

public class Bullet {
    // Una RectF representa el tamaño y la localizacion de la bala
    private RectF mRect;
    // Para medir y controlar la velocidad de la bala en la pantalla
    private float mXVelocity;
    private float mYVelocity;
    // Tamaño de la bala
    private float mWidth;
    private float mHeight;

    // Constructor
    public Bullet(int screenX){
        // El tamaño de la bala en función al tamaño de la pantalla
        mWidth = screenX / 100;
        mHeight = screenX / 100;
        mRect = new RectF();
        mYVelocity = (screenX / 5);
        mXVelocity = (screenX / 5);
    }

    // Regresa la referencia a RectF para calculo de colisiones
    RectF getRect(){
        return mRect;
    }

    // Mover la bala basada en la velocidad y el fps
    void update(long fps){
        // Update the left and top coordinates
        // based on the velocity and current frame rate
        mRect.left = mRect.left + (mXVelocity / fps);
        mRect.top = mRect.top + (mYVelocity / fps);
        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top - mHeight;
    }

    // Revertir las direcciones de la bala
    void reverseYVelocity(){
        mYVelocity = -mYVelocity;
    }
    void reverseXVelocity(){
        mXVelocity = -mXVelocity;
    }

    // Generar una nueva bala, con los datos en mRect
    void spawn(int pX, int pY, int vX, int vY){
        // Spawn the bullet at the location
        // passed in as parameters
        mRect.left = pX;
        mRect.top = pY;
        mRect.right = pX + mWidth;
        mRect.bottom = pY + mHeight;
        // Head away from the player
        // It's only fair
        mXVelocity = mXVelocity * vX;
        mYVelocity = mYVelocity * vY;

    }
}
