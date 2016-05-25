package pt.up.fe.alpha.labtablet.utils;

import android.view.View;

public interface OnItemClickListener {
    void onItemClick(View view, int position);
    void onItemLongClick(View view, int position);
}