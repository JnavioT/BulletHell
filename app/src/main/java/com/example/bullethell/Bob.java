package com.example.bullethell;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class Bob {
    RectF mRect;
    float mBobHeight;
    float mBobWidth;
    boolean mTeleporting = false;
    Bitmap mBitmap;

    public Bob(Context context, float screenX, float screenY) {
        mBobHeight = screenY / 10;
        mBobWidth = mBobHeight / 2;
        mRect = new RectF(screenX / 2, screenY / 2,
                (screenX / 2) + mBobWidth,
                (screenY / 2) + mBobHeight);

        // Preparar el bitmap con la imagen .png
        mBitmap = BitmapFactory.decodeResource
                (context.getResources(), R.drawable.bob2);
    }

    boolean teleport(float newX, float newY){
        boolean success = false;
        // Mover al personaje cuando no se este teltransportando
        if(!mTeleporting){
            //newX y newY representan las coordenadas donde se hizo click
            // se actualiza en función a ello
            mRect.left = newX - mBobWidth / 2;
            mRect.top = newY - mBobHeight / 2;
            mRect.bottom = mRect.top + mBobHeight;
            mRect.right = mRect.left + mBobWidth;
            mTeleporting = true;
        // Retorna que se realizo la operacion exitosa a  BulletHellGame
            success = true;
        }
        return success;
    }

    void setTelePortAvailable(){
        mTeleporting = false;
    }
    // Retorna la referencia de mRect para calculo de colisiones
    RectF getRect(){
        return mRect;
    }
    // Retorna una referencia del bitmap
    Bitmap getBitmap(){
        return mBitmap;
    }

}
