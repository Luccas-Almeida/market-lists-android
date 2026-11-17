package com.LuccasAlmeida.marketlists;

import android.os.Bundle;
import android.text.Html;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class SlideActivity extends AppCompatActivity {

    private ViewPager view_pager;
    private SliderAdapter sliderAdapter;
    private LinearLayout layout_dots;
    private TextView[] tv_dost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        view_pager = findViewById(R.id.view_pager);
        layout_dots = findViewById(R.id.layout_dots);

        sliderAdapter = new SliderAdapter(SlideActivity.this);
        view_pager.setAdapter(sliderAdapter);

        addDots(0);

        view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addDots(int position)
    {
        tv_dost = new TextView[3];
        layout_dots.removeAllViews();
        for (int i = 0; i < tv_dost.length; i++)
        {
            tv_dost[i] = new TextView(SlideActivity.this);
            tv_dost[i].setText(Html.fromHtml("&#8226;"));
            tv_dost[i].setTextSize(35);
            tv_dost[i].setTextColor(getResources().getColor(R.color.light_white));

            layout_dots.addView(tv_dost[i]);
        }

        if (tv_dost.length > 0)
        {
            tv_dost[position].setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

}