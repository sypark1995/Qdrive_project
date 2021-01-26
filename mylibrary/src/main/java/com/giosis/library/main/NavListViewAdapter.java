package com.giosis.library.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giosis.library.R;

import java.util.ArrayList;

public class NavListViewAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final ArrayList<NavListItem> itemArrayList = new ArrayList<>();

    public NavListViewAdapter(Context context) {

        this.context = context;
    }

    @Override
    public Object getGroup(int i) {
        return itemArrayList.get(i);
    }

    @Override
    public int getGroupCount() {
        return itemArrayList.size();
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup viewGroup) {

        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_nav_list, null);
        } else {
            view = convertView;
        }


        ImageView img_nav_list_icon = view.findViewById(R.id.img_nav_list_icon);
        TextView text_nav_list_title = view.findViewById(R.id.text_nav_list_title);
        ImageView img_nav_list_arrow_img = view.findViewById(R.id.img_nav_list_arrow_img);

        //
        NavListItem item = itemArrayList.get(position);

        text_nav_list_title.setText(item.getTitle());

        switch (position) {
            case 1:     // SCAN
                if (isExpanded) {

                    img_nav_list_icon.setBackgroundResource(R.drawable.qdrive_side_scan_h);
                    text_nav_list_title.setTextColor(context.getResources().getColor(R.color.color_4fb648));
                    img_nav_list_arrow_img.setImageResource(R.drawable.qdrive_side_arrow_up);
                } else {

                    img_nav_list_icon.setBackground(item.getIcon());
                    text_nav_list_title.setTextColor(context.getResources().getColor(R.color.color_303030));
                    img_nav_list_arrow_img.setImageResource(R.drawable.qdrive_side_arrow);
                }
                img_nav_list_arrow_img.setVisibility(View.VISIBLE);
                break;
            case 2:     // LIST
                if (isExpanded) {

                    img_nav_list_icon.setBackgroundResource(R.drawable.qdrive_side_list_h);
                    text_nav_list_title.setTextColor(context.getResources().getColor(R.color.color_4fb648));
                    img_nav_list_arrow_img.setImageResource(R.drawable.qdrive_side_arrow_up);
                } else {

                    img_nav_list_icon.setBackground(item.getIcon());
                    text_nav_list_title.setTextColor(context.getResources().getColor(R.color.color_303030));
                    img_nav_list_arrow_img.setImageResource(R.drawable.qdrive_side_arrow);
                }
                img_nav_list_arrow_img.setVisibility(View.VISIBLE);
                break;
            default:
                img_nav_list_icon.setBackground(item.getIcon());
                text_nav_list_title.setTextColor(context.getResources().getColor(R.color.color_303030));
                img_nav_list_arrow_img.setVisibility(View.GONE);
                break;
        }

        //
        FrameLayout layout_navi_list_item_bottom = view.findViewById(R.id.layout_navi_list_item_bottom);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (position == itemArrayList.size() - 1) {

            lp.setMargins(0, 0, 0, 0);
        } else {

            float density = context.getResources().getDisplayMetrics().density;
            int paddingDp = 20;
            int paddingPixel = (int) (paddingDp * density);

            lp.setMargins(paddingPixel, 0, 0, 0);
        }

        layout_navi_list_item_bottom.setLayoutParams(lp);


        return view;
    }


    @Override
    public int getChildrenCount(int i) {

        if (itemArrayList.get(i).getChildArrayList() == null) {
            return 0;
        }

        return itemArrayList.get(i).getChildArrayList().size();
    }


    @Override
    public Object getChild(int i, int i1) {
        return itemArrayList.get(i).getChildArrayList().get(i1);
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup viewGroup) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_nav_list_child, null);
        } else {

            view = convertView;
        }

        LinearLayout layout_nav_list_sub = view.findViewById(R.id.layout_nav_list_sub);
        TextView text_nav_list_sub_title = view.findViewById(R.id.text_nav_list_sub_title);


        float density = context.getResources().getDisplayMetrics().density;

        int paddingDp = 20;
        int paddingPixel = (int) (paddingDp * density);
        int paddingDp2 = 12;
        int paddingPixel2 = (int) (paddingDp2 * density);

        if (i1 == 0) {
            layout_nav_list_sub.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
        } else {
            layout_nav_list_sub.setPadding(paddingPixel, paddingPixel2, paddingPixel, paddingPixel);
        }

        text_nav_list_sub_title.setText(itemArrayList.get(i).getChildArrayList().get(i1));


        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


    public void addItem(Drawable icon, String title, ArrayList<String> list, int position) {

        NavListItem item = new NavListItem();

        item.setIcon(icon);
        item.setTitle(title);
        item.setChildArrayList(list);

        if (position != -1) {
            itemArrayList.add(position, item);
        } else {
            itemArrayList.add(item);
        }
    }

    public NavListItem getItem(int position) {

        return itemArrayList.get(position);
    }
}