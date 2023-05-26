package com.gachon.i_edu.adpater;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.gachon.i_edu.R;

//import static androidx.appcompat.graphics.drawable.DrawableContainerCompat.Api21Impl.getResources;

public class TextViewPagerAdapter extends PagerAdapter {

    private Context context = null;

    int[] images ={R.drawable.lim, R.drawable.log, R.drawable.ln};

    // Context 를 전달받아 context 에 저장하는 생성자 추가.
    public TextViewPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup fragment_container, int position) {
        // position 값을 받아 주어진 위치에 페이지를 생성한다

        View view = null;

        if(context != null) {
            // LayoutInflater 를 통해 "/res/layout/page.xml" 을 뷰로 생성.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.fragment_banner, fragment_container, false);
            ImageView imageView = (ImageView)view.findViewById(R.id.title);
            //imageView.setImageResource(ResourcesCompat.getDrawable(getResources(),R.drawable.book_org, null));
            imageView.setImageResource(images[position]);
        }

        // 뷰페이저에 추가
        fragment_container.addView(view);

        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup fragment_container, int position, @NonNull Object object) {
        // position 값을 받아 주어진 위치의 페이지를 삭제한다
        fragment_container.removeView((View) object);
    }

    @Override
    public int getCount() {
        // 사용 가능한 뷰의 개수를 return 한다
        // 전체 페이지 수는 10개로 고정한다
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        // 페이지 뷰가 생성된 페이지의 object key 와 같은지 확인한다
        // 해당 object key 는 instantiateItem 메소드에서 리턴시킨 오브젝트이다
        // 즉, 페이지의 뷰가 생성된 뷰인지 아닌지를 확인하는 것
        return view == object;
    }
}