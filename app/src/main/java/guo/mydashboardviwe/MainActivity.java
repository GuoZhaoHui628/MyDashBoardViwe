package guo.mydashboardviwe;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import guo.mydashboardviwe.view.MyDashBoardView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MyDashBoardView dashView;
    private boolean isChangeColor = true;
    private SeekBar sbChangeCurrentvalue,sbChangeMaxValue;
    private TextView tv1,tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = (TextView) this.findViewById(R.id.tv1);
        tv2 = (TextView) this.findViewById(R.id.tv2);

        sbChangeCurrentvalue = (SeekBar) this.findViewById(R.id.sb_change_currentvalue);
        sbChangeMaxValue = (SeekBar) this.findViewById(R.id.sb_change_maxValue);

        dashView = (MyDashBoardView) this.findViewById(R.id.dashview);
        sbChangeMaxValue.setMax(300);
        sbChangeCurrentvalue.setMax((int)(dashView.getmMax()));
        this.findViewById(R.id.bt_change_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isChangeColor){
                    dashView.setDashColor(Color.parseColor("#39B54A"));
                    isChangeColor = false;
                }else{
                    dashView.setDashColor(Color.parseColor("#DA6858"));
                    isChangeColor = true;
                }
            }
        });

        sbChangeCurrentvalue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int currentValue = seekBar.getProgress();
                dashView.setCurrentValue(currentValue);
                tv1.setText("当前值:"+currentValue+"/"+seekBar.getMax());
                Log.d(TAG,"    --------currentValue---------   "+currentValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbChangeMaxValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value = seekBar.getProgress()+100;
                dashView.setmMax(value);
                tv2.setText("最大值:"+value+"/"+seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}
