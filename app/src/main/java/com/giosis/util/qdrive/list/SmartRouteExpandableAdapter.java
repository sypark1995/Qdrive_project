package com.giosis.util.qdrive.list;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * @author krm0219
 **/
public class SmartRouteExpandableAdapter extends BaseExpandableListAdapter {
    static String TAG = "SmartRouteExpandableAdapter";

    private Context context;

    private ArrayList<SmartRouteResult.RouteMaster> routeMasterArrayList;
    private CustomExpandableAdapter routeDetailAdapter;


    public SmartRouteExpandableAdapter(Context mContext, ArrayList<SmartRouteResult.RouteMaster> routeMasterArrayList) {

        this.context = mContext;        // getActivity()
        this.routeMasterArrayList = routeMasterArrayList;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        final SmartRouteResult.RouteMaster routeMasterItem = routeMasterArrayList.get(groupPosition);
        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_smart_route_master, null);
        } else {

            view = convertView;
        }

        RelativeLayout layout_route_master_item = view.findViewById(R.id.layout_route_master_item);
        final TextView text_route_master_name = view.findViewById(R.id.text_route_master_name);
        ImageView img_route_master_google = view.findViewById(R.id.img_route_master_google);
        final ImageView img_route_master_arrow = view.findViewById(R.id.img_route_master_arrow);
        final WebView webview_route_master_map = view.findViewById(R.id.webview_route_master_map);


        if (groupPosition == 0) {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);
            layout_route_master_item.setLayoutParams(lp);
        } else {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 30, 0, 0);
            layout_route_master_item.setLayoutParams(lp);
        }


        if (isExpanded) {

            layout_route_master_item.setBackgroundResource(R.drawable.custom_background_card_view_selected);
            img_route_master_arrow.setBackgroundResource(R.drawable.qdrive_side_arrow_up);
        } else {

            layout_route_master_item.setBackgroundResource(R.drawable.custom_background_card_view);
            img_route_master_arrow.setBackgroundResource(R.drawable.qdrive_side_arrow);
        }


        text_route_master_name.setText(routeMasterItem.getRouteName());


        img_route_master_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                webview_route_master_map.setWebViewClient(new MyWebClient());
                webview_route_master_map.getSettings().setLoadsImagesAutomatically(true);
                webview_route_master_map.getSettings().setJavaScriptEnabled(true);
                webview_route_master_map.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                webview_route_master_map.loadUrl(routeMasterItem.getGoogleURL());
            }
        });


        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_smart_route_detail, null);
        } else {

            view = convertView;
        }

        final ExpandableListView exlist_route_detail = view.findViewById(R.id.exlist_route_detail);
        TextView text_route_orders_not = view.findViewById(R.id.text_route_orders_not);

        // 다른 item 의 데이터가 남아 보여지는거 막기 위함.
        ViewGroup.LayoutParams params = exlist_route_detail.getLayoutParams();
        params.height = 0;
        exlist_route_detail.setLayoutParams(params);
        exlist_route_detail.requestLayout();

        exlist_route_detail.setVisibility(View.GONE);
        text_route_orders_not.setVisibility(View.GONE);


        if (routeMasterArrayList.get(groupPosition).getRouteDetailList() != null) {

            int listSize = routeMasterArrayList.get(groupPosition).getRouteDetailList().size();
            Log.e("krm0219", TAG + "  Smart Route Child Size : " + routeMasterArrayList.get(groupPosition).getRouteDetailList().size());

            if (listSize == 0) {

                exlist_route_detail.setVisibility(View.GONE);
                text_route_orders_not.setVisibility(View.VISIBLE);
            } else {

                exlist_route_detail.setVisibility(View.VISIBLE);
                text_route_orders_not.setVisibility(View.GONE);

                routeDetailAdapter = new CustomExpandableAdapter(context, routeMasterArrayList.get(groupPosition).getRouteDetailList());
                exlist_route_detail.setAdapter(routeDetailAdapter);
            }
        }


        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                progressDialog.dismiss();
                setExpandableListViewHeight(exlist_route_detail, "click");
            }
        }, 200);


        exlist_route_detail.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (parent.isGroupExpanded(groupPosition)) {

                    // expanded 되어있는 item click > collapse
                    setExpandableListViewHeight(parent, "collapse");
                }
                return false;
            }
        });

        exlist_route_detail.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                int groupCount = routeDetailAdapter.getGroupCount();

                // 한 그룹을 클릭하면 나머지 그룹들은 닫힌다.
                for (int i = 0; i < groupCount; i++) {
                    if (!(i == groupPosition))
                        exlist_route_detail.collapseGroup(i);
                }

                setExpandableListViewHeight(exlist_route_detail, "expand");
            }
        });


        return view;
    }


    // ListView 동적으로 item 추가시 height 구해서 제공
    private static void setExpandableListViewHeight(ExpandableListView listView, String status) {

        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();

        if (listAdapter == null) {
            return;
        }


        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        View view = null;

        for (int i = 0; i < listAdapter.getGroupCount(); i++) {

            view = listAdapter.getGroupView(i, false, view, listView);

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

            // open > close : isGroupExpanded (true)  // close > open : isGroupExpanded (false)
            if (status.equals("expand") && listView.isGroupExpanded(i)) {

                View listItem = null;

                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {

                    listItem = listAdapter.getChildView(i, j, false, listItem, listView);
                    listItem.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, View.MeasureSpec.UNSPECIFIED));
                    listItem.measure(
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    totalHeight += listItem.getMeasuredHeight();
                }
            }
        }


        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;

        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    @Override
    public int getGroupCount() {
        if (routeMasterArrayList != null && routeMasterArrayList.size() > 0) {
            return routeMasterArrayList.size();
        }

        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return routeMasterArrayList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        if (routeMasterArrayList.get(groupPosition).getRouteDetailList() != null) {
            ArrayList<RowItem> routeDetailArrayList = routeMasterArrayList.get(groupPosition).getRouteDetailList();
            return routeDetailArrayList.get(childPosition);
        }

        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    private class MyWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Intent intent = parse(url);
            if (isIntent(url)) {
                if (isExistInfo(intent, context) || isExistPackage(intent, context)) {

                    return start(intent, context);
                } else {

                    gotoMarket(intent, context);
                }
            } else if (isMarket(url)) {

                return start(intent, context);
            }

            return false;
        }

        private Intent parse(String url) {

            try {
                return Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        private Boolean isIntent(String url) {
            return url.matches("^intent:?\\w*://\\S+$");
        }

        private Boolean isMarket(String url) {
            return url.matches("^market://\\S+$");
        }

        private Boolean isExistInfo(Intent intent, Context context) {
            try {
                return intent != null && context.getPackageManager().getPackageInfo(intent.getPackage(), PackageManager.GET_ACTIVITIES) != null;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        private Boolean isExistPackage(Intent intent, Context context) {
            return intent != null && context.getPackageManager().getLaunchIntentForPackage(intent.getPackage()) != null;
        }

        private boolean start(Intent intent, Context context) {

            context.startActivity(intent);
            return true;
        }

        private boolean gotoMarket(Intent intent, Context context) {

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + intent.getPackage())));
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);
        }
    }
}