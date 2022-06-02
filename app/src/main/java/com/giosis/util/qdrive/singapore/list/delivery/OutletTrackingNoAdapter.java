package com.giosis.util.qdrive.singapore.list.delivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.util.DisplayUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class OutletTrackingNoAdapter extends BaseAdapter {
    String TAG = "OutletTrackingNoAdapter";

    Context context;
    ArrayList<OutletDeliveryItem> trackingNoList;
    String route;

    public OutletTrackingNoAdapter(Context context, ArrayList<OutletDeliveryItem> list, String route) {

        this.context = context;
        this.trackingNoList = list;
        this.route = route;

        if (route.contains("7E")) {

            Collections.sort(trackingNoList, new CompareNameAsc());
            resetListItem();
        } else if (route.contains("FL")) {

            Collections.sort(trackingNoList, new CompareTrackingNoAsc());
        }
    }

    public void resetListItem() {

        ArrayList<OutletDeliveryItem> qrcodeListItem = new ArrayList<>();

        for (int i = 0; i < trackingNoList.size(); i++) {

            if (i == 0) {

                OutletDeliveryItem item = new OutletDeliveryItem();
                item.setTrackingNo("1");
                item.setJobID(trackingNoList.get(0).getJobID());
                item.setVendorCode(trackingNoList.get(0).getVendorCode());
                item.setQrCode(trackingNoList.get(0).getQrCode());

                qrcodeListItem.add(item);
            }

            if (i + 1 < trackingNoList.size()) {

                if (!trackingNoList.get(i).getJobID().equals(trackingNoList.get(i + 1).getJobID())) {

                    OutletDeliveryItem item = new OutletDeliveryItem();
                    item.setTrackingNo("1");
                    item.setJobID(trackingNoList.get(i + 1).getJobID());
                    item.setVendorCode(trackingNoList.get(i + 1).getVendorCode());
                    item.setQrCode(trackingNoList.get(i + 1).getQrCode());

                    qrcodeListItem.add(item);
                }
            }
        }

        for (int i = 0; i < qrcodeListItem.size(); i++) {
            trackingNoList.add(qrcodeListItem.get(i));
        }

        Collections.sort(trackingNoList, new CompareNameAsc());
    }


    @Override
    public int getCount() {

        if (trackingNoList != null) {
            return trackingNoList.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return trackingNoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final OutletDeliveryItem item = trackingNoList.get(position);
        View view = null;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                view = inflater.inflate(R.layout.outlet_qrcode_item, null);

                final RelativeLayout layout_sign_d_outlet_qrcode_load = view.findViewById(R.id.layout_sign_d_outlet_qrcode_load);
                TextView text_sign_d_outlet_qrcode_date = view.findViewById(R.id.text_sign_d_outlet_qrcode_date);
                TextView text_sign_d_outlet_qrcode_job_id = view.findViewById(R.id.text_sign_d_outlet_qrcode_job_id);
                TextView text_sign_d_outlet_qrcode_vendor_code = view.findViewById(R.id.text_sign_d_outlet_qrcode_vendor_code);
                final ImageView img_sign_d_outlet_qrcode = view.findViewById(R.id.img_sign_d_outlet_qrcode);

                final LinearLayout layout_sign_d_outlet_qrcode_reload = view.findViewById(R.id.layout_sign_d_outlet_qrcode_reload);
                Button btn_sign_d_outlet_reload = view.findViewById(R.id.btn_sign_d_outlet_reload);

                btn_sign_d_outlet_reload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        QRCodeAsyncTask qrCodeAsyncTask = new QRCodeAsyncTask(layout_sign_d_outlet_qrcode_load, layout_sign_d_outlet_qrcode_reload, img_sign_d_outlet_qrcode, item.getQrCode());
                        qrCodeAsyncTask.execute();
                    }
                });

                text_sign_d_outlet_qrcode_date.setText(item.getJobID().substring(2, 6) + "-" + item.getJobID().substring(6, 8) + "-" + item.getJobID().substring(8, 10));
                text_sign_d_outlet_qrcode_job_id.setText(item.getJobID());
                text_sign_d_outlet_qrcode_vendor_code.setText(item.getVendorCode());

                QRCodeAsyncTask qrCodeAsyncTask = new QRCodeAsyncTask(layout_sign_d_outlet_qrcode_load, layout_sign_d_outlet_qrcode_reload, img_sign_d_outlet_qrcode, item.getQrCode());
                qrCodeAsyncTask.execute();


        }

        return view;
    }

    // Federated Locker - Tracking No Sort
    class CompareTrackingNoAsc implements Comparator<OutletDeliveryItem> {

        @Override
        public int compare(OutletDeliveryItem o1, OutletDeliveryItem o2) {

            return o1.getTrackingNo().compareTo(o2.getTrackingNo());
        }
    }

    private int dpTopx(Context context, float dp) {
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return pixel;
    }


    // 리스트 정렬. 1순위 Job ID / 2순위 Tracking No
    class CompareNameAsc implements Comparator<OutletDeliveryItem> {

        @Override
        public int compare(OutletDeliveryItem o1, OutletDeliveryItem o2) {

            if (o1.getJobID().equals(o2.getJobID())) {

                return o1.getTrackingNo().compareTo(o2.getTrackingNo());
            } else {

                return o1.getJobID().compareTo(o2.getJobID());
            }
        }
    }


    public class QRCodeAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        RelativeLayout layout_sign_d_outlet_qrcode_load;
        LinearLayout layout_sign_d_outlet_qrcode_reload;
        ImageView img_sign_d_outlet_qrcode;
        String imgUrl;
        ProgressDialog progressDialog;

        public QRCodeAsyncTask(RelativeLayout relativeLayout, LinearLayout linearLayout, ImageView imageView, String imgUrl) {

            this.layout_sign_d_outlet_qrcode_load = relativeLayout;
            this.layout_sign_d_outlet_qrcode_reload = linearLayout;
            this.img_sign_d_outlet_qrcode = imageView;
            this.imgUrl = imgUrl;

            progressDialog = new ProgressDialog(imageView.getContext());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(imageView.getContext().getResources().getString(R.string.text_please_wait));
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            try {

                URL url = new URL(imgUrl);
                trustAllHosts();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                return myBitmap;
            } catch (Exception e) {

                Log.e("Exception", TAG + "   QRCode to Bitmap Exception : " + e.toString());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            DisplayUtil.dismissProgressDialog(progressDialog);

            if (bitmap != null) {

                layout_sign_d_outlet_qrcode_load.setVisibility(View.VISIBLE);
                layout_sign_d_outlet_qrcode_reload.setVisibility(View.GONE);

                img_sign_d_outlet_qrcode.setImageBitmap(bitmap);
            } else {

                layout_sign_d_outlet_qrcode_load.setVisibility(View.GONE);
                layout_sign_d_outlet_qrcode_reload.setVisibility(View.VISIBLE);
            }
        }

        private void trustAllHosts() {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            }};

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}