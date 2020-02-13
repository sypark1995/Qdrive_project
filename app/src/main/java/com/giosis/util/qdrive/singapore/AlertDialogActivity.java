package com.giosis.util.qdrive.singapore;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.NotificationManagerCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.giosis.util.qdrive.main.MainActivity;
import com.giosis.util.qdrive.util.DataUtil;

public class AlertDialogActivity extends Activity {

    private String notiMessage;
    private String notiTitle;
    private String actionKey;
    private String actionValue;

    private String outletPush = "N";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setFinishOnTouchOutside(false);

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_push_alert);

        TextView text_dialog_title = findViewById(R.id.text_dialog_title);
        TextView text_dialog_message = findViewById(R.id.text_dialog_message);
        Button btn_dialog_ok = findViewById(R.id.btn_dialog_ok);


        Bundle bun = getIntent().getExtras();

        notiMessage = bun.getString("notiMessage");
        notiTitle = bun.getString("notiTitle");
        actionKey = bun.getString("actionKey");
        actionValue = bun.getString("actionValue");

        try {

            outletPush = bun.getString("outletPush");
        } catch (Exception e) {

            outletPush = "N";
        }

        if (!notiTitle.equals("") && !notiTitle.equals("QSign SG")) {

            text_dialog_title.setVisibility(View.VISIBLE);
            text_dialog_title.setText(notiTitle);
        } else {

            text_dialog_title.setVisibility(View.GONE);
        }

        text_dialog_message.setText(notiMessage);
        btn_dialog_ok.setOnClickListener(clickListener);
    }

    @Override
    public void onBackPressed() {
    }

    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (outletPush != null && outletPush.equals("Y")) {

                Intent intent = new Intent(AlertDialogActivity.this, MainActivity.class);
                intent.putExtra("outletPush", outletPush);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else if (actionKey.equals("LAE")) {      // 2019.02

                try {
                    if (actionValue != null) {

                        // ok 버튼 눌러서 이동하면 Notification 지우기
                        NotificationManagerCompat.from(getApplication()).cancel(Integer.parseInt(actionValue.substring(0, 9)));
                    }
                } catch (Exception e) {

                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(DataUtil.locker_pin_url));
                startActivity(intent);
            }

            finish();
        }
    };
}

