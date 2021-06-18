package com.accurascan.accura.qatar.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.accurascan.accura.qatar.demo.util.Utils;
import com.accurascan.libqatar.CameraView;
import com.accurascan.libqatar.interfaces.OcrCallback;
import com.accurascan.libqatar.model.OcrData;
import com.accurascan.libqatar.model.RecogResult;
import com.accurascan.libqatar.motiondetection.SensorsActivity;
import com.accurascan.libqatar.util.AccuraLog;
import com.docrecog.scan.RecogEngine;
import com.docrecog.scan.RecogType;

import java.lang.ref.WeakReference;

public class OcrActivity extends SensorsActivity implements OcrCallback {

    private static final String TAG = OcrActivity.class.getSimpleName();
    private static final int RESULT_ACTIVITY_CODE = 101;
    private CameraView cameraView;
    private View viewLeft, viewRight, borderFrame;
    private TextView tvTitle, tvScanMessage;
    private ImageView imageFlip;
    private int cardId,countryId;
    private String cardName;
    RecogType recogType;
    private LinearLayout data_table;

    private static class MyHandler extends Handler {
        private final WeakReference<OcrActivity> mActivity;

        public MyHandler(OcrActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            OcrActivity activity = mActivity.get();
            if (activity != null) {
                String s = "";
                if (msg.obj instanceof String) s = (String) msg.obj;
                switch (msg.what) {
                    case 0: activity.tvTitle.setText(s);break;
                    case 1: activity.tvScanMessage.setText(s);break;
                    case 2: if (activity.cameraView != null) activity.cameraView.flipImage(activity.imageFlip);
                        activity.handler.sendEmptyMessageDelayed(3, 1000);
                        break;
                    case 3:
                        if (activity.data_table != null) {
                            activity.data_table.removeAllViews();
                        }
                    default: break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private Handler handler = new MyHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoActionBar);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the window title.
        setContentView(R.layout.ocr_activity);
        AccuraLog.loge(TAG, "Start Camera Activity");
        init();

        recogType = RecogType.detachFrom(getIntent());
        cardId = getIntent().getIntExtra("card_id", 0);
        countryId = getIntent().getIntExtra("country_id", 0);
        cardName = getIntent().getStringExtra("card_name");

        AccuraLog.loge(TAG, "RecogType " + recogType);
        AccuraLog.loge(TAG, "Card Id " + cardId);
        AccuraLog.loge(TAG, "Country Id " + countryId);

        initCamera();
    }

    private void initCamera() {
        AccuraLog.loge(TAG, "Initialized camera");
        //<editor-fold desc="To get status bar height">
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarTop = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int statusBarHeight = contentViewTop - statusBarTop;
        //</editor-fold>

        RelativeLayout linearLayout = findViewById(R.id.ocr_root); // layout width and height is match_parent

        cameraView = new CameraView(this);
        if (recogType == RecogType.OCR) {
            // must have to set data for RecogType.OCR
            cameraView.setCountryId(countryId).setCardId(cardId);
        }
        cameraView.setRecogType(recogType)
                .setMinFrameForValidate(3) // support only odd numbers of frame
                .setView(linearLayout) // To add camera view
                .setOcrCallback(this)  // To get Update and Success Call back
                .setStatusBarHeight(statusBarHeight)  // To remove Height from Camera View if status bar visible
                .setAPIData(Utils.DATABASE_SERVER_URL, Utils.DATABASE_SERVER_KEY)
//                optional field
//                .setEnableMediaPlayer(false) // false to disable sound and true to enable sound and default it is true
//                .setCustomMediaPlayer(MediaPlayer.create(this, com.accurascan.ocr.mrz.R.raw.beep)) // To add your custom sound and Must have to enable media player
                .init();  // initialized camera
    }

    private void init() {
        viewLeft = findViewById(R.id.view_left_frame);
        viewRight = findViewById(R.id.view_right_frame);
        borderFrame = findViewById(R.id.border_frame);
        tvTitle = findViewById(R.id.tv_title);
        tvScanMessage = findViewById(R.id.tv_scan_msg);
        imageFlip = findViewById(R.id.im_flip_image);
        data_table = findViewById(R.id.data_table);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (cameraView != null) cameraView.onWindowFocusUpdate(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null) cameraView.onResume();
    }

    @Override
    protected void onPause() {
        if (cameraView != null) cameraView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        AccuraLog.loge(TAG, "onDestroy");
        if (cameraView != null) cameraView.onDestroy();
        super.onDestroy();
        Runtime.getRuntime().gc(); // to clear garbage
    }

    /**
     * Override method call after camera initialized successfully
     *
     * And update your border frame according to width and height
     * it's different for different card
     *
     * Call {@link CameraView#startOcrScan(boolean isReset)} To start Camera Preview
     *
     * @param width    border layout width
     * @param height   border layout height
     */
    @Override
    public void onUpdateLayout(int width, int height) {
        AccuraLog.loge(TAG, "Frame Size (wxh) : " + width + "x" +  height);
        if (cameraView != null) cameraView.startOcrScan(false);

        //<editor-fold desc="To set camera overlay Frame">
        ViewGroup.LayoutParams layoutParams = borderFrame.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        borderFrame.setLayoutParams(layoutParams);
        ViewGroup.LayoutParams lpRight = viewRight.getLayoutParams();
        lpRight.height = height;
        viewRight.setLayoutParams(lpRight);
        ViewGroup.LayoutParams lpLeft = viewLeft.getLayoutParams();
        lpLeft.height = height;
        viewLeft.setLayoutParams(lpLeft);

        findViewById(R.id.ocr_frame).setVisibility(View.VISIBLE);
        //</editor-fold>
    }

    /**
     * Override this method after scan complete to get data from document
     *
     * @param result is scanned card data
     *  result instance of {@link OcrData} if recog type is {@link com.docrecog.scan.RecogType#OCR}
     *  result instance of {@link RecogResult} if recog type is {@link com.docrecog.scan.RecogType#MRZ}
     *
     */
    @Override
    public void onScannedComplete(Object result) {
        AccuraLog.loge(TAG, "onScannedComplete: ");
        Intent intent = new Intent(this, OcrResultActivity.class);
        if (result != null) {
            if (result instanceof OcrData) {
                /**
                 * @recogType is {@link com.docrecog.scan.RecogType#OCR}*/
                OcrData.setOcrResult((OcrData) result);
                RecogType.OCR.attachTo(intent);
                startActivityForResult(intent, RESULT_ACTIVITY_CODE);
            } else if (result instanceof RecogResult) {
                /**
                 *  @recogType is {@link com.docrecog.scan.RecogType#MRZ}*/
                RecogResult.setRecogResult((RecogResult) result);
                RecogType.MRZ.attachTo(intent);
                startActivityForResult(intent, RESULT_ACTIVITY_CODE);
            }
        } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param titleCode to display scan card message on top of border Frame
     *
     * @param errorMessage To display process message.
     *                null if message is not available
     * @param isFlip  To set your customize animation after complete front scan
     */
    @Override
    public void onProcessUpdate(int titleCode, String errorMessage, boolean isFlip) {
        AccuraLog.loge(TAG, "onProcessUpdate :-> " + titleCode + "," + errorMessage + "," + isFlip);
        Message message;
        if (getTitleMessage(titleCode) != null) {
            /**
             *
             * 1. Scan Frontside of Card Name // for front side ocr
             * 2. Scan Backside of Card Name // for back side ocr
             * 4. Scan Front Side of Document // for front side MRZ
             * 5. Now Scan Back Side of Document // for back side MRZ
             */

            message = new Message();
            message.what = 0;
            message.obj = getTitleMessage(titleCode);
            handler.sendMessage(message);
        }
        if (errorMessage != null) {
            message = new Message();
            message.what = 1;
            message.obj = getErrorMessage(errorMessage);
            handler.sendMessage(message);
        }
        if (isFlip) {
            message = new Message();
            message.what = 2;
            handler.sendMessage(message);//  to set default animation or remove this line to set your customize animation
        }

    }

    private String getTitleMessage(int titleCode) {
        if (titleCode < 0) return null;
        switch (titleCode){
            case RecogEngine.SCAN_TITLE_OCR_FRONT:// for front side ocr;
                return String.format("Scan Front Side of %s", cardName);
            case RecogEngine.SCAN_TITLE_OCR_BACK: // for back side ocr
                isAdded = false;
                return String.format("Scan Back Side of %s", cardName);
            case RecogEngine.SCAN_TITLE_MRZ:// for front side MRZ
                return "Scan Front Side of Document";
            default: return "";
        }
    }

    private String getErrorMessage(String s){
        switch (s){
            case RecogEngine.ACCURA_ERROR_CODE_MOTION:
                return "Keep Document Steady";
            case RecogEngine.ACCURA_ERROR_CODE_DOCUMENT_IN_FRAME:
                return "Keep Qatar ID in frame";
            case RecogEngine.ACCURA_ERROR_CODE_BRING_DOCUMENT_IN_FRAME:
                return "Bring card near to frame.";
            case RecogEngine.ACCURA_ERROR_CODE_PROCESSING:
                return "Processing...";
            case RecogEngine.ACCURA_ERROR_CODE_BLUR_DOCUMENT:
                return "Blur detect in document";
            case RecogEngine.ACCURA_ERROR_CODE_FACE_BLUR:
                return "Blur detected over face";
            case RecogEngine.ACCURA_ERROR_CODE_GLARE_DOCUMENT:
                return "Glare detect in document";
            case RecogEngine.ACCURA_ERROR_CODE_HOLOGRAM:
                return "Hologram Detected";
            case RecogEngine.ACCURA_ERROR_CODE_DARK_DOCUMENT:
                return "Low lighting detected";
            case RecogEngine.ACCURA_ERROR_CODE_PHOTO_COPY_DOCUMENT:
                return "Can not accept Photo Copy Document";
            case RecogEngine.ACCURA_ERROR_CODE_FACE:
                return "Face not detected";
            case RecogEngine.ACCURA_ERROR_CODE_MRZ:
                return "MRZ not detected";
            case RecogEngine.ACCURA_ERROR_CODE_PASSPORT_MRZ:
                return "Passport MRZ not detected";
            case RecogEngine.ACCURA_ERROR_CODE_RETRYING:
                return "Retrying";
            case RecogEngine.ACCURA_ERROR_CODE_WRONG_SIDE:
                return "Scanning wrong side of document";
            case RecogEngine.ACCURA_ERROR_CODE_UPSIDE_DOWN_SIDE:
                return "Document is upside down. Place it properly";
            case RecogEngine.ACCURA_ERROR_CODE_MOVE_CARD:
                return "Move your phone/card a little";
            default:
                return s;
        }
    }

    boolean isAdded = false;

    /**
     * To know recognize process while scanning card
     *
     * @param requiredFields  An array of required fields to scanned card
     * @param recognizedFields An array of scanned data from card
     */
    @Override
    public void onRecognizeProcess(String[] requiredFields, String[] recognizedFields) {
        if (!isAdded) {
            data_table.removeAllViews();
            for (int j = 0; j < requiredFields.length; j++) {
                if (requiredFields[j] != null) {
                    View layout = LayoutInflater.from(OcrActivity.this).inflate(R.layout.item_process, null);
                    ImageView imCheck = layout.findViewById(R.id.im_check);
                    TextView tvField = layout.findViewById(R.id.tv_field);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (1.0f/requiredFields.length)
                    );
                    tvField.setText(requiredFields[j]);
                    imCheck.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cb_uncheck, null));
                    layout.setLayoutParams(param);
                    for (int i = 0; i < recognizedFields.length; i++) {
                        if (recognizedFields[i] != null && requiredFields[j].equals(recognizedFields[i])) {
                            imCheck.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cb_check, null));
                        }
                    }
                    data_table.addView(layout);
                    isAdded = true;
                }
            }
        } else
            for (int i = 0; i < recognizedFields.length; i++) {
                if (recognizedFields[i] != null) {
                    for (int j = 0; j < requiredFields.length; j++) {
                        if (requiredFields[j] != null && requiredFields[j].equals(recognizedFields[i]) && data_table.getChildAt(j) != null) {
                            LinearLayout layout = (LinearLayout) data_table.getChildAt(j);
                            ImageView imCheck = layout.findViewById(R.id.im_check);
                            imCheck.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cb_check, null));
                        }
                    }
                }
            }
    }

    @Override
    public void onError(final String errorMessage) {
        // stop ocr if failed
//        tvScanMessage.setText(errorMessage);
        Runnable runnable = () -> Toast.makeText(OcrActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        runOnUiThread(runnable);
    }

    @Override
    public void onAPIError(String errorMessage) {
        Runnable runnable = () -> Toast.makeText(OcrActivity.this, "Failed to send data on server -> " + errorMessage, Toast.LENGTH_LONG).show();
        runOnUiThread(runnable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_ACTIVITY_CODE) {
                Runtime.getRuntime().gc(); // To clear garbage
                //<editor-fold desc="Call CameraView#startOcrScan(true) To start again Camera Preview
                //And CameraView#startOcrScan(false) To start first time">

                isAdded = false;
                if (cameraView != null) cameraView.startOcrScan(true);
                data_table.removeAllViews();
                //</editor-fold>
            }
        }
    }

}