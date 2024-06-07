package com.example.searchyourstuffeasily;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FurnitureDeploy extends AppCompatActivity {
    ImageButton furnitureImg;
    LinearLayout TopLinear, BottomLinear;
    static final String IMAGEBUTTON_TAG = "드래그 이미지";

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_deploying);

        furnitureImg = (ImageButton) findViewById(R.id.Image1);
        TopLinear = (LinearLayout) findViewById(R.id.TopLinear);
        BottomLinear = (LinearLayout) findViewById(R.id.BottomLinear);

        furnitureImg.setTag(IMAGEBUTTON_TAG);
        furnitureImg.setOnLongClickListener(this::LongClickListener);

        TopLinear.setOnDragListener(this::DragListener);
        BottomLinear.setOnDragListener(this::DragListener);
    }

    private boolean DragListener(View view, DragEvent dragEvent) {
        Drawable normalShape = getResources().getDrawable(R.color.teal_700);
        Drawable targetShape = getResources().getDrawable(R.color.purple_200);
        TextView textView = (TextView) findViewById(R.id.dropText);

        switch(dragEvent.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
                Log.d("DragClickListener", "ACTION_DRAG_STARTED");
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Log.d("DragClickListener", "ACTION_DRAG_ENTERED");
                view.setBackground(targetShape);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                Log.d("DragClickListener", "ACTION_DRAG_EXITED");
                view.setBackground(normalShape);
                break;
            case DragEvent.ACTION_DROP:
                Log.d("DragClickListener", "ACTION_DROP");
                if(view.getId() == R.id.BottomLinear){
                    View localView = (View) dragEvent.getLocalState();
                    ViewGroup viewGroup = (ViewGroup) localView.getParent();
                    LinearLayout containView = (LinearLayout) view;

                    viewGroup.removeView(localView);
                    textView.setText("이미지가 드랍되었습니다.");
                    containView.addView(localView);
                    localView.setVisibility(View.VISIBLE);
                }else if(view.getId() == R.id.TopLinear){
                    View localView = (View) dragEvent.getLocalState();
                    ViewGroup viewGroup= (ViewGroup) localView.getParent();
                    LinearLayout containView = (LinearLayout) view;

                    viewGroup.removeView(localView);
                    containView.addView(localView);
                    localView.setVisibility(View.VISIBLE);
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d("DragClickListener", "ACTION_DRAG_ENDED");
                view.setBackground(normalShape);
            default:
                break;
        }
        return true;
    }

    public boolean LongClickListener(View view) {
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

        ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

        view.startDrag(data, shadowBuilder, view, 0);
        view.setVisibility(View.INVISIBLE);
        return true;
    }


}
