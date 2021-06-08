package com.example.bullethell;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;

public class BulletHellGame extends SurfaceView implements Runnable {

    // Para hacer pruebas en pantalla
    boolean mDebugging = true;
    // Objetos para el bucle del juego
    private Thread mGameThread = null;
    private volatile boolean mPlaying;
    private boolean mPaused = true;
    // Objetos para dibujar
    private SurfaceHolder mOurHolder;
    private Canvas mCanvas;
    private Paint mPaint;
    // Para mantener el seguimiento de la tasa de frames
    private long mFPS;
    // Constante para calculos de tiempo
    private final int MILLIS_IN_SECOND = 1000;
    // Para guardar el tamaño de la pantalla
    private int mScreenX;
    private int mScreenY;

    // Para el tamaño del texto en pantalla
    private int mFontSize;
    private int mFontMargin;
    // Para efectos de sonido
    private SoundPool mSP;
    private int mBeepID = -1;
    private int mTeleportID = -1;

    // Juego permite hasta 10000 balas
    private Bullet[] mBullets = new Bullet[10000];
    private int mNumBullets = 0;
    private int mSpawnRate = 1;
    private Random mRandomX = new Random();
    private Random mRandomY = new Random();

    // Objetos del personaje y sus caracteristicas en el juego
    private Bob mBob;
    private boolean mHit = false;
    private int mNumHits;
    private int mShield = 10;

    // Para medir tiempos del juego
    private long mStartGameTime;
    private long mBestGameTime;
    private long mTotalGameTime;


    // Método constructor que es llamado desde BulletHellActivity
    public BulletHellGame(Context context, int x, int y) {
        super(context);
        mScreenX = x;
        mScreenY = y;
        // Font y Margin en función al tamaño de la pantalla
        mFontSize = mScreenX / 20;
        mFontMargin = mScreenX / 50;
        mOurHolder = getHolder();
        mPaint = new Paint();
        // Para inicializar los métodos de sonido
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC,
                    0);
        }
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("beep.ogg");
            mBeepID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd("teleport.ogg");
            mTeleportID = mSP.load(descriptor, 0);
        } catch (IOException e) {
            Log.e("error", "fallo la carga de sonidos");
        }
        for(int i = 0; i < mBullets.length; i++){
            mBullets[i] = new Bullet(mScreenX);
        }

        mBob = new Bob(context, mScreenX, mScreenY);

        startGame();
    }

    // Llamada a nuevo juego
    public void startGame(){
        mNumHits = 0;
        mNumBullets = 0;

        mHit = false;

        if(mTotalGameTime > mBestGameTime){
            mBestGameTime = mTotalGameTime;
        }

    }
    // Aparicion de nuevas balas
    private void spawnBullet(){
        // se aumenta en 1 el num de balas
        mNumBullets++;
        // Se genera la nueva bala y se le da la orientacion tal que no colisione facilmente con el personaje
        int spawnX;
        int spawnY;
        int velocityX;
        int velocityY;

        // No aparezca cerca del personaje
        if (mBob.getRect().centerX() < mScreenX / 2) {
            // Personaje esta a la izquierda entonces la bala aparece a la derecha
            spawnX = mRandomX.nextInt(mScreenX / 2) + mScreenX / 2;
            // Y con sentido a la derecha
            velocityX = 1;
        } else {
            // Personaje esta a la derecha entonces la bala aparece a la izquierda
            spawnX = mRandomX.nextInt(mScreenX / 2);
            //  con sentido a la izquierda
            velocityX = -1;
        }
        // No aparezca cerca del personaje , arriba y abajo
        if (mBob.getRect().centerY() < mScreenY / 2) {
            // Personaje esta arriba
            spawnY = mRandomY.nextInt(mScreenY / 2) + mScreenY / 2;
            // Bala con setnido hacia abajo
            velocityY = 1;
        } else {
            // Personaje esta abajo
            spawnY = mRandomY.nextInt(mScreenY / 2);
            // Bala con setnido hacia arriba
            velocityY = -1;

        }

        // Se crea la bala con los valores indicados
        mBullets[mNumBullets - 1].spawn(spawnX, spawnY, velocityX, velocityY);
    }

    // Manejo del bucle del juego
    @Override
    public void run() {
        while (mPlaying) {
            long frameStartTime = System.currentTimeMillis();
            if (!mPaused) {
                update();
                // Detectamos colisiones ya que las balas se han movido en update()
                detectCollisions();
            }
            draw();
            long timeThisFrame = System.currentTimeMillis() - frameStartTime;
            if (timeThisFrame >= 1) {
                mFPS = MILLIS_IN_SECOND / timeThisFrame;
            }
        }
    }
    // Update actualiza la posición de las balas
    private void update(){
        for(int i = 0; i < mNumBullets; i++){
            mBullets[i].update(mFPS);
        }
    }
    // Detección de colisiones
    private void detectCollisions(){
        // Se verifica para todas las balas si han chocado con los límites de la pantalla
        for(int i = 0; i < mNumBullets; i++) {
            if (mBullets[i].getRect().bottom > mScreenY) {
                mBullets[i].reverseYVelocity();
            }
            else if (mBullets[i].getRect().top < 0) {
                mBullets[i].reverseYVelocity();
            }
            else if (mBullets[i].getRect().left < 0) {
                mBullets[i].reverseXVelocity();
            }
            else if (mBullets[i].getRect().right > mScreenX) {
                mBullets[i].reverseXVelocity();
            }
        }
        // Se verifica si las balas han chocado con el personaje
        for (int i = 0; i < mNumBullets; i++) {
            // Sí has sido chocado
            if (RectF.intersects(mBullets[i].getRect(), mBob.getRect())) {
                // Se emite un pitido
                mSP.play(mBeepID, 1, 1, 0, 0, 1);
                // Se habilita la bandera del choque
                mHit = true;
                // Se hace que la bala cambie de dirección
                mBullets[i].reverseXVelocity();
                mBullets[i].reverseYVelocity();
                // Se guarda el número de choques
                mNumHits++;
                // Se verifica que la protección del personaje no se haya acabado
                if (mNumHits == mShield) {
                    mPaused = true;
                    mTotalGameTime = System.currentTimeMillis() - mStartGameTime;
                    startGame();
                }
            }

        }
    }

    private void draw(){
        if (mOurHolder.getSurface().isValid()) {
            mCanvas = mOurHolder.lockCanvas();
            mCanvas.drawColor(Color.argb(255, 243, 111, 36));
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            //Se dibujan las balas
            for(int i = 0; i < mNumBullets; i++) {
                mCanvas.drawRect(mBullets[i].getRect(), mPaint);
            }
            mCanvas.drawBitmap(mBob.getBitmap(), mBob.getRect().left, mBob.getRect().top, mPaint);

            mPaint.setTextSize(mFontSize);
            mCanvas.drawText("Balas: " + mNumBullets +
                            " Blindaje: " + (mShield - mNumHits) +
                            " Mejor Tiempo: " + mBestGameTime / MILLIS_IN_SECOND,
                        mFontMargin, mFontSize, mPaint);

            // Si se pausa no escribir el tiempo
            if(!mPaused) {
                mCanvas.drawText("Segundos sobrevividos: " +
                                ((System.currentTimeMillis() - mStartGameTime) / MILLIS_IN_SECOND),
                        mFontMargin, 150, mPaint);
            }

            if(mDebugging) {
                printDebuggingText();
            }
            mOurHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Cuando se toca la pantalla
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Se regresa al juego
                if(mPaused){
                    mStartGameTime = System.currentTimeMillis();
                    mPaused = false;
                }
                // se teleporta el personaje
                if(mBob.teleport(motionEvent.getX(), motionEvent.getY())){
                    mSP.play(mTeleportID, 1, 1, 0, 0, 1);
                }
                break;
                // Al levantar el mouse se genera nueva bala
            case MotionEvent.ACTION_UP:
                mBob.setTelePortAvailable();
                spawnBullet();
                break;
        }
        return true;
    }
    //ciclo de pausa
    public void pause() {
        mPlaying = false;
        try {
            mGameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "hilo detenido");
        }
    }

    public void resume() {
        mPlaying = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }
    private void printDebuggingText(){
        int debugSize = 35;
        int debugStart = 150;
        mPaint.setTextSize(debugSize);
        mCanvas.drawText("FPS: " + mFPS ,
                10, debugStart + debugSize, mPaint);
    }


}
