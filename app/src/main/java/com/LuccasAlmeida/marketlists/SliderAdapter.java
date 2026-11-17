package com.LuccasAlmeida.marketlists;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    private Context context;

    public SliderAdapter(Context context) {
        this.context = context;
    }

    private int[] slider_images = {
            R.drawable.slide1,
            R.drawable.slide2,
            R.drawable.slide3
    };
    public String[] slider_title = {
            "Praticidade na sua mão",
            "Controle seus gastos",
            "Inicie já"
    };
    private String[] slide_desc = {
            "Organize suas compras de forma inteligente.",
            "Acompanhe o total estimado e itens pendentes.",
            ""
    };

    @Override
    public int getCount() {
        return slider_title.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView img_banner = view.findViewById(R.id.img_banner);
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_desc = view.findViewById(R.id.tv_desc);
        Button btn_entrar = view.findViewById(R.id.btn_entrar);

        img_banner.setImageResource(slider_images[position]);
        tv_title.setText(slider_title[position]);
        tv_desc.setText(slide_desc[position]);

        if (position == slider_title.length - 1) {
            btn_entrar.setVisibility(View.VISIBLE);
            btn_entrar.setOnClickListener(v -> {
                SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("onboarding_completed", true).apply();

                Intent intent = new Intent(context, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        } else {
            btn_entrar.setVisibility(View.INVISIBLE);
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}