package pt.up.fe.labtablet.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.HomeTip;


public class HomeTipsAdapter extends RecyclerView.Adapter<HomeTipsAdapter.TipItemVH> {

    private final List<HomeTip> items;
    private final Context context;

    public HomeTipsAdapter(Context context,
            List<HomeTip> srcItems) {

        this.context = context;
        this.items = srcItems;

    }

    @Override
    public TipItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_home_tip, parent, false);

        return new TipItemVH(v);
    }

    @Override
    public void onBindViewHolder(TipItemVH holder, int position) {
        HomeTip item = items.get(position);
        holder.tipTitle.setText(item.getTitle());
        holder.tipBody.setText(Html.fromHtml(item.getBody()));
        if (!item.getResourceID().equals("")) {
            int id = context.getResources().getIdentifier(item.getResourceID(), "drawable", context.getPackageName());
            if (id != 0) {
                Drawable drawable = context.getResources().getDrawable(id);
                holder.tipDrawable.setImageDrawable(drawable);
                holder.tipDrawable.setVisibility(View.VISIBLE);
            } else {
                holder.tipDrawable.setVisibility(View.GONE);
            }
        } else {
            holder.tipDrawable.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public class TipItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView tipTitle;
        public final TextView tipBody;
        public final ImageView tipDrawable;

        public TipItemVH(View itemView) {
            super(itemView);
            tipTitle = (TextView) itemView.findViewById(R.id.tip_header);
            tipBody = (TextView) itemView.findViewById(R.id.tip_body);
            tipDrawable = (ImageView) itemView.findViewById(R.id.tip_drawable);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
