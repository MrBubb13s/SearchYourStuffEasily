package com.example.searchyourstuffeasily;

import static com.example.searchyourstuffeasily.ImageDraw.CurrentShape;
import static com.example.searchyourstuffeasily.ImageDraw.LINE;
import static com.example.searchyourstuffeasily.ImageDraw.RECT;
import static com.example.searchyourstuffeasily.ImageDraw.ERAS;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DrawablePainter extends View {
    ArrayList<CurrentShapeInfo> myShapeArrayList = new ArrayList<>();
    CurrentShapeInfo MyShape;
    Paint paint = new Paint();

    int startX, startY = -1;
    int endX, endY = -1;
    private int countRect;

    public DrawablePainter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        paint.setTextSize(50);
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public DrawablePainter(Context context){
        super(context);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        paint.setTextSize(50);
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int X = (int)event.getX();
        int Y = (int)event.getY();

        switch (event.getAction()){
            case(MotionEvent.ACTION_DOWN):
                startX = X; startY = Y;
                break;
            case(MotionEvent.ACTION_UP):
                endX = X; endY = Y;
                invalidate();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);

        //화면에 직사각형 또는 선을 그릴 때마다 해당 정보를 CurrentShapeInfo로 저장한 뒤 CurrentShapeInfo를 자료형으로 갖는 ArrayList에 추가함.
        //onDraw가 실행될 때마다 사각형의 갯수를 세는 countRect가 0으로 초기화되고 ArrayList안에 저장된 RECT 형태의 갯수에 맞게 조정되어 화면에 그려지는 사각형의 갯수와 동일한 번호를 부여함
        MyShape = new CurrentShapeInfo(CurrentShape, startX, startY, endX, endY, paint);
        myShapeArrayList.add(MyShape);
        countRect = 0;

        //화면에 여러 페인트를 동시에 그릴 수 있도록 ArrayList를 활용하여 정보를 저장함.
        for(CurrentShapeInfo cs: myShapeArrayList) {
            if(cs.shape_type == RECT)
                countRect++;
            drawShape(cs, canvas);
        }
        if(MyShape != null)
            drawShape(MyShape, canvas);
    }

    //메뉴에서 선과 사각형을 선택하여 그에 알맞은 페인트를 캔버스에 그려내는 함수 (선과 사각형 추가일자: 24/04/04) (초기화버튼 추가일자: 24/04/08)
    public void drawShape(CurrentShapeInfo cs, Canvas canvas){
        switch(cs.shape_type){
            case LINE:
                canvas.drawLine(cs.startXInfo, cs.startYInfo, cs.endXInfo, cs.endYInfo, cs.paintInfo);
                break;
            case RECT:
                Rect rect = new Rect(cs.startXInfo, cs.startYInfo, cs.endXInfo, cs.endYInfo);
                canvas.drawRect(rect, cs.paintInfo);
                canvas.drawText( cs.nameInfo + String.valueOf(countRect), (cs.endXInfo + cs.startXInfo)/2 , (cs.endYInfo + cs.startYInfo)/2, cs.paintInfo);
                break;
            //초기화버튼: 선택 후 캔버스를 한번만 터치하면 캔버스 사이즈와 동일한 흰 사각형으로 뒤덮음과 동시에 그린 방의 개수를 초기화함.
            //floatingButton 저장버튼 클릭 시 캐시에 저장과 동시에 현 정보를 ERAS를 선택한 것으로 바꾸어 화면을 초기화함. (일자:24/04/11)
            case ERAS:
                countRect = 0;
                Rect eraser = new Rect(0,0, canvas.getWidth(), canvas.getHeight());
                canvas.drawColor(Color.WHITE);
                canvas.drawRect(eraser, paint);
        }
    }

    public Bitmap getCurrentCanvas() {
        Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);

        return bitmap;
    }

    //현재 화면에 입력된 정보를 저장하는 클래스, ArrayList를 이용해 해당 정보들을 그린 갯수만큼 저장함.
    private static class CurrentShapeInfo {
        int shape_type, startXInfo, startYInfo, endXInfo, endYInfo;
        String nameInfo;
        Paint paintInfo;

        public CurrentShapeInfo(int shape_type, int startX, int startY, int endX, int endY, Paint paintInfo){
            this.shape_type = shape_type;
            this.startXInfo = startX;
            this.startYInfo = startY;
            this.endXInfo = endX;
            this.endYInfo = endY;
            this.nameInfo = "방 ";
            this.paintInfo = paintInfo;
        }

        public CurrentShapeInfo(int shape_type, int startX, int startY, int endX, int endY, String nameInfo, Paint paintInfo){
            this.shape_type = shape_type;
            this.startXInfo = startX;
            this.startYInfo = startY;
            this.endXInfo = endX;
            this.endYInfo = endY;
            this.nameInfo = nameInfo;
            this.paintInfo = paintInfo;
        }
    }
}