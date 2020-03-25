package com.example.xgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

public class playground extends SurfaceView implements View.OnTouchListener {

    private static  int WIDTH = 40;
    private static  final int COL = 10;
    private static final  int ROW = 10;
    private static final  int BLOCKS = 10;//默认添加的障碍数量
    private Dot matrix[][];
    private Dot cat;
    public playground(Context context) {
        super(context);
        getHolder().addCallback(callback);
        matrix = new Dot[ROW][COL];
        for (int i = 0 ; i<ROW;i++){
            for (int j =0 ;j<COL;j++){
                matrix[i][j] = new Dot(j,i);
            }
        }
        setOnTouchListener(this);
        initGame();
    }
    private Dot getDot(int x,int y){
        return matrix[y][x];
    }

    private boolean isAtEdge(Dot d){
        if(d.getX()*d.getY() == 0||d.getX()+1 ==COL ||d.getY()+1==ROW){
             return true;
        }
        return false;
    }

    private Dot getNrighbor(Dot d,int dir){
        switch (dir){
            case 1:
                return  getDot(d.getX()-1,d.getY());
            case 2:
                if (d.getY()%2 ==0){
                  return   getDot(d.getX()-1,d.getY()-1);
                }else {
                   return getDot(d.getX(),d.getY()-1);
                }
            case 3:
                if (d.getY()%2 ==0){
                  return  getDot(d.getX(),d.getY()-1);
                }else {
                  return  getDot(d.getX()+1,d.getY()-1);
                }
            case 4:
                return getDot(d.getX()+1,d.getY());
            case 5:
                if (d.getY()%2 ==0){
                 return   getDot(d.getX(),d.getY()+1);
                }else {
                 return   getDot(d.getX()+1,d.getY()+1);
                }
            case 6:
                if (d.getY()%2 ==0){
                 return   getDot(d.getX()-1,d.getY()+1);
                }else {
                 return   getDot(d.getX(),d.getY()+1);
                }
                default:
                    break;
        }
        return null;
    }

    private int getDistance(Dot one,int dir){
        int distance = 0;
        if (isAtEdge(one)){
            return 1;
        }
        Dot ori,next;
        ori = one;
        while (true){
          next = getNrighbor(ori,dir);
          if (next.getStatus()==Dot.STATUS_ON){
              return distance*-1;
          }
          if (isAtEdge(next)){
              distance++;
              return distance;
          }
          distance++;
          ori = next;
        }
    }
    private void MoveTo(Dot one){
        one.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(),cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(one.getX(),one.getY());
    }

    private void move(){
        if(isAtEdge(cat)){
         lose();
         return;
        }
        Vector<Dot> avalible =new Vector<>();
        Vector<Dot> positive =new Vector<>();
        HashMap<Dot,Integer>postivelength = new HashMap<>();
        for (int i = 1;i<7;i++){
            Dot n = getNrighbor(cat,i);
            if (n.getStatus()==Dot.STATUS_OFF){
                avalible.add(n);
                postivelength.put(n,i);
                if(getDistance(n,i) >0){
                    positive.add(n);
                }
            }
        }
        if (avalible.size() == 0){
            win();
        }else if(avalible.size() ==1){
                MoveTo(avalible.get(0));
        }else {
            Dot best = null;
            if(positive.size() != 0){
                System.out.println("向前进");
               int min = 9999;
               for (int i = 0; i<positive.size();i++){
                   int a = getDistance(positive.get(i),postivelength.get(positive.get(i)));
                   if(a<min){
                       min = a;
                       best = positive.get(i);
                   }
               }
            }else {
                System.out.println("躲路障");
                int max = 0;
              for (int i = 0; i<avalible.size();i++){
                  int k = getDistance(avalible.get(i),postivelength.get(avalible.get(i)));
                  if(k <= max){
                      max = k;
                      best = avalible.get(i);
                  }
              }
            }
            System.out.println(best);
            MoveTo(best);
        }
    }

    private void lose(){
       Toast.makeText(getContext(),"lose",Toast.LENGTH_SHORT).show();
    }
    private void win(){
        Toast.makeText(getContext(),"you win",Toast.LENGTH_SHORT).show();
    }
    public void  redraw(){
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.LTGRAY);
        canvas.drawColor(Color.CYAN);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0 ;i<ROW;i++){
            int offset = 0;
            if(i%2!=0){
                offset = WIDTH/2;
            }
            for (int j = 0;j<COL;j++){
                Dot one = getDot(j,i);
                switch (one.getStatus()){
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFF0000);
                        break;
                }
                canvas.drawOval(new RectF(one.getX()*WIDTH+offset,one.getY()*WIDTH,(one.getX()+1)*WIDTH+offset,(one.getY()+1)*WIDTH),paint);
            }
        }
        getHolder().unlockCanvasAndPost(canvas);
    }
    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
           WIDTH = width/(COL+1);
           redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };
    private void  initGame(){
        for (int i =0;i<ROW;i++){
            for (int j = 0;j<COL;j++){
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat = new Dot(4,5);
        getDot(4,5).setStatus(Dot.STATUS_IN);
        for (int i = 0;i<BLOCKS;){
            int x = (int)(Math.random()*1000)%COL;
            int y = (int)(Math.random()*1000)%ROW;
            if (getDot(x,y).getStatus() == Dot.STATUS_OFF){
                getDot(x,y).setStatus(Dot.STATUS_ON);
                i++;
                System.out.println("blocks :"+i);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if(e.getAction()==MotionEvent.ACTION_UP){
           // Toast.makeText(getContext(),e.getX()+" "+e.getY(),Toast.LENGTH_SHORT).show();
            int x,y;
            y = (int)(e.getY()/WIDTH);
            if(y%2==0){
              x = (int)(e.getX()/WIDTH);
            }else {
                x = (int)((e.getX()-WIDTH/2)/WIDTH);
            }
            if(x+1>COL || y+1>ROW){
               initGame();
            }else if(getDot(x,y).getStatus()==Dot.STATUS_OFF){
               getDot(x,y).setStatus(Dot.STATUS_ON);
               move();
            }
            redraw();
        }
        return true;
    }
}
