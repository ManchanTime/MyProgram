package com.gachon.i_edu;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class PhOffsetItemDecoration extends RecyclerView.ItemDecoration {

    private final int mPadding;

    public PhOffsetItemDecoration(int a_padding) {
        mPadding = a_padding;
    }

    @Override
    public void getItemOffsets(Rect a_outRect, View a_view, RecyclerView a_parent, RecyclerView.State a_state) {
        super.getItemOffsets(a_outRect, a_view, a_parent, a_state);
        a_outRect.top = mPadding;
        a_outRect.bottom = mPadding;
        a_outRect.left = mPadding;
        a_outRect.right = mPadding;
    }
}