/**
 *
 */
package gmkt.inc.android.common.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

import gmkt.inc.android.common.util.GMKT_Utils;

/**
 *
 * @brief Indeterminate Loading ProgressBar Dialog
 * @author wontae
 * @date 2011. 07. 26
 * @version 1.0.0
 *
 */
public class GMKT_ProgressDialog extends Dialog {

    // 문구, 배경 색상
    // 문구 300px을 넘어갈 경우 End 축약처리
    // 문구 없을 경우 ProgressBar위치 가운데로 이동
    // 문구 폰트 설정 (색상, 크기, Style - Normal, bold)
    public static final int PADDING = 30;
    public static final float[] OUT_RADII = new float[]{10, 10, 10, 10, 10, 10, 10, 10};
    public static final int ROUND_RECT_HEIGHT = 135;
    public static final int DIALOG_TITLE_HEIGHT = 45;
    public static final int DIALOG_MAX_WIDTH = 300;
    public static final float TEXT_DEFAULT_SIZE = 10.0f;

    public static final int PROGRESS_LOADING_WITH_MESSAGE_TOP_MARGIN = 20;
    public static final int PROGRESS_LOADING_WITHOUT_MESSAGE_TOP_MARGIN = 32;

    private TextView mMessageTextView = null;
    private ProgressBar mProgressBar = null;
    private ImageView mBackgroundImageView = null;

    /**
     * @brief Indeterminate Loading ProgressBar Dialog 생성자
     * @param    context    Dialog를 보여줄 Context
     */
    public GMKT_ProgressDialog(Context context) {
        super(context);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    /**
     * @brief Indeterminate Loading ProgressBar Dialog 생성자(테마설정)
     * @param    context    Dialog를 보여줄 Context
     * @param    theme        Dialog의 테마 설정
     */
    public GMKT_ProgressDialog(Context context, int themeID,
                               CharSequence title, CharSequence message,
                               boolean indeterminate,
                               int backgroundColor, int fontColor,
                               boolean boldStyle, float fontSize) {

        super(context, themeID);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        initDialog(context, title, message, indeterminate, backgroundColor, fontColor, boldStyle, fontSize);

    }

    private void initDialog(Context context, CharSequence title,
                            CharSequence message, boolean indeterminate,
                            int backgroundColor,
                            int fontColor, boolean boldStyle, float fontSize) {
        // Message TextView 설정
        String strMessage = "";

        if (message == null) {
            strMessage = "";
        } else {
            strMessage = message.toString();
        }

        mMessageTextView = makeMessageTextView(context, strMessage, fontColor, boldStyle, fontSize);

        // MessageTextView의 넓이를 이용해 Background Image의 넓이를 자동으로 조절
        int roundRectImageWidth = getMessageTextViewWidth(mMessageTextView);
        int roundRectImageHeight = getRoundRectImageHeight(fontSize);
        int progressBarTopMargin = getProgressLoadingTopMargin(mMessageTextView);

        // 라운딩 처리된 Image 생성 후 ImageView에 설정
        mBackgroundImageView = makeRoundRecImageView(context, roundRectImageWidth, roundRectImageHeight, backgroundColor);

        // Progress Bar 생성
        mProgressBar = makeProgressBar(context, progressBarTopMargin);

        // Progress Bar와 텍스트 문구를 포함하는 Layout
        LinearLayout progressTextLayout = new LinearLayout(context);
        progressTextLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        progressTextLayout.setOrientation(LinearLayout.VERTICAL);
        progressTextLayout.setGravity(Gravity.CENTER);
        progressTextLayout.addView(mProgressBar);
        progressTextLayout.addView(mMessageTextView);

        // 배경위에 ProgressBar Text Layout을 포함하는 Container Layout
        FrameLayout containerLayout = new FrameLayout(context);
        containerLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.TOP));
        containerLayout.addView(mBackgroundImageView);
        containerLayout.addView(progressTextLayout);

        // Dialog에 Background & ProgressBar & Message 추가
        addContentView(containerLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        //getWindow().getAttributes().y -= DIALOG_TITLE_HEIGHT; // Dialog Title 높이가 45px
    }


    private TextView makeMessageTextView(Context context, String strMessage,
                                         int fontColor, boolean boldStyle, float fontSize) {

        TextView messageTextView = new TextView(context);
        messageTextView.setPadding(PADDING, 0, PADDING, 0);
        messageTextView.setText(strMessage);
        messageTextView.setTextColor(fontColor);
        messageTextView.setTextSize(fontSize);
        messageTextView.setMaxLines(1);
        messageTextView.setMaxWidth(DIALOG_MAX_WIDTH);
        messageTextView.setSingleLine(true);
        messageTextView.setEllipsize(TruncateAt.END);

        if (boldStyle) {
            messageTextView.setTypeface(Typeface.DEFAULT_BOLD);
            messageTextView.setPaintFlags(messageTextView.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        }

        // Message Layout Param 설정
        LinearLayout.LayoutParams messageTextViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        messageTextView.setLayoutParams(messageTextViewParams);

        return messageTextView;
    }

    private int getMessageTextViewWidth(TextView messageTextView) {

        int messageTextViewWidth = 0;
        String strMessage = messageTextView.getText().toString();

        if (strMessage != null && !strMessage.equals("")) {
            Rect bounds = new Rect();
            Paint textPaint = messageTextView.getPaint();
            textPaint.getTextBounds(strMessage, 0, strMessage.length(), bounds);
            messageTextViewWidth = bounds.width() + messageTextView.getPaddingLeft() + messageTextView.getPaddingRight();
            if (messageTextViewWidth > DIALOG_MAX_WIDTH) {
                messageTextViewWidth = DIALOG_MAX_WIDTH;
            }
        } else {
            // null || "" 일 경우 처리
            messageTextViewWidth = ROUND_RECT_HEIGHT;
            messageTextView.setWidth(ROUND_RECT_HEIGHT);
        }

        return messageTextViewWidth;
    }

    private int getProgressLoadingTopMargin(TextView messageTextView) {
        int progressBarTopMargin = 0;
        String strMessage = messageTextView.getText().toString();

        if (strMessage != null && !strMessage.equals("")) {
            progressBarTopMargin = PROGRESS_LOADING_WITH_MESSAGE_TOP_MARGIN;
        } else {
            // null || "" 일 경우 처리
            progressBarTopMargin = PROGRESS_LOADING_WITHOUT_MESSAGE_TOP_MARGIN;
        }

        return progressBarTopMargin;
    }

    private ShapeDrawable makeRoundRectDrawableImage(int width, int height, int backgroundColor) {
        ShapeDrawable rndrect = new ShapeDrawable(new RoundRectShape(OUT_RADII, null, null));
        rndrect.setIntrinsicHeight(height);
        rndrect.setIntrinsicWidth(width);
        rndrect.getPaint().setColor(backgroundColor);

        return rndrect;
    }

    private ImageView makeRoundRecImageView(Context context, int roundRectImageWidth, int roundRectImageHeight, int backgroundColor) {
        // 라운딩 처리된 Image 생성 후 ImageView에 설정
        ShapeDrawable rndrect = makeRoundRectDrawableImage(roundRectImageWidth, roundRectImageHeight, backgroundColor);

        ImageView bgImageView = new ImageView(context);
        bgImageView.setImageDrawable(rndrect);

        return bgImageView;
    }

    private ProgressBar makeProgressBar(Context context, int progressBarTopMargin) {
        // Progress Bar 생성
        ProgressBar progressBar = new ProgressBar(context);

        // Progress Bar 레이아웃 파라미터 설정
        LinearLayout.LayoutParams progressBarParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        progressBarParams.topMargin = progressBarTopMargin;
        progressBar.setLayoutParams(progressBarParams);

        return progressBar;
    }

    private int getRoundRectImageHeight(float fontSize) {
        int size = ROUND_RECT_HEIGHT + (int) (fontSize - TEXT_DEFAULT_SIZE);
        size = GMKT_Utils.DPFromPixel(getContext(), size);
        return size;
    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message) {
        return show(context, title, message, false);
    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate,
                                           boolean cancelable, OnCancelListener cancelListener) {

        return show(context, title, message, indeterminate, cancelable, cancelListener, Color.argb(230, 100, 100, 100));

    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate,
                                           boolean cancelable, OnCancelListener cancelListener, int backgroundColor) {

        return show(context, title, message, indeterminate, cancelable, cancelListener, backgroundColor, Color.WHITE);

    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate,
                                           boolean cancelable, OnCancelListener cancelListener,
                                           int backgroundColor, int fontColor) {

        return show(context, title, message, indeterminate, cancelable, cancelListener, backgroundColor, fontColor, false);

    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate,
                                           boolean cancelable, OnCancelListener cancelListener,
                                           int backgroundColor, int fontColor, boolean boldStyle) {

        return show(context, title, message, indeterminate, cancelable, cancelListener, backgroundColor, fontColor, boldStyle, 12.0f);

    }

    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate,
                                           boolean cancelable, OnCancelListener cancelListener,
                                           int backgroundColor, int fontColor,
                                           boolean boldStyle, float fontSize) {

        return show(context, title, message, indeterminate, cancelable, cancelListener, backgroundColor, fontColor, boldStyle, fontSize, false);

    }


    /**
     * @brief Indeterminate Loading ProgressBar Dialog 설정 후 Dialog 반환
     * @param context                Activity Context
     * @param title                    타이틀
     * @param message                메세지
     * @param indeterminate
     * @param cancelable            Dialog Cancel처리 가능 여부
     * @param cancelListener        Dialog Cancel처리 리스너
     * @param backgroundColor        Dilalog의 라운드 처리된 배경용 사각이미지의 색상
     * @param fontColor                Message의 글자 색상
     * @param boldStyle                Message의 Bold 처리 여부
     * @param fontSize                Message의 Font Size
     * @param dimEnabled            Dialog이외의 배경 Dim처리
     * @return Indeterminate Loading ProgressBar Dialog
     */
    public static GMKT_ProgressDialog show(Context context, CharSequence title,
                                           CharSequence message, boolean indeterminate,
                                           boolean cancelable, OnCancelListener cancelListener,
                                           int backgroundColor, int fontColor,
                                           boolean boldStyle, float fontSize, boolean dimEnabled) {

        // Progress 로딩 Dialog 생성
        int sytleResId = R.style.GMKT_ProgressDialog;

        if (dimEnabled) {
            sytleResId = R.style.GMKT_ProgressDialogDimEabled;
        }

        GMKT_ProgressDialog dialog = new GMKT_ProgressDialog(context, sytleResId,
                title, message, indeterminate,
                backgroundColor,
                fontColor, boldStyle, fontSize);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();

        return dialog;
    }

}
