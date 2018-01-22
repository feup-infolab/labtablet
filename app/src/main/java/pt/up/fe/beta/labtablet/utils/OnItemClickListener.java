package pt.up.fe.beta.labtablet.utils;

import android.view.View;

public interface OnItemClickListener {
    void onItemClick(View view, int position);
    void onItemLongClick(View view, int position);
    void onDeleteRequested(View view, int position);
}