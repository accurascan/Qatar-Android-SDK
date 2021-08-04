package com.accurascan.accura.qatar.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.accurascan.accura.qatar.demo.util.Utils;
import com.accurascan.facedetection.LivenessCustomization;
import com.accurascan.facedetection.SelfieCameraActivity;
import com.accurascan.facedetection.model.AccuraVerificationResult;
import com.accurascan.facematch.util.BitmapHelper;
import com.accurascan.facematch.util.FaceHelper;
import com.accurascan.libqatar.model.OcrData;
import com.accurascan.libqatar.model.RecogResult;
import com.bumptech.glide.Glide;
import com.docrecog.scan.RecogType;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;

import java.io.File;

public class OcrResultActivity extends BaseActivity implements FaceHelper.FaceMatchCallBack, FaceCallback {

    private final int ACCURA_LIVENESS_CAMERA = 101;

    TableLayout mrz_table_layout, front_table_layout, back_table_layout;
    ImageView ivUserProfile, ivUserProfile2, iv_frontside, iv_backside;
    LinearLayout ly_back, ly_front;
    View ly_mrz_container, ly_front_container, ly_back_container;
    View loutImg, loutImg2;
    private FaceHelper faceHelper;
    private TextView tvFaceMatchScore, tvLivenessScore;
    private View loutFmScore, loutLivenessScore;
    OcrData.MapData Frontdata;
    OcrData.MapData Backdata;
    private Bitmap face1,face2;
    private String kycId = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoActionBar);
        setContentView(R.layout.activity_ocr_result);

        initUI();

        if (RecogType.detachFrom(getIntent()) == RecogType.OCR) {
            OcrData ocrData = OcrData.getOcrResult();
            if (ocrData == null) {
                return;
            }
            kycId = ocrData.getKycId();
            setOcrData(ocrData);
        } else if (RecogType.detachFrom(getIntent()) == RecogType.MRZ) {
            RecogResult g_recogResult = RecogResult.getRecogResult();
            if (g_recogResult == null) {
                return;
            }
            kycId = g_recogResult.kycId;
            if (g_recogResult.faceBitmap != null) {
                face1 = g_recogResult.faceBitmap;
            }
            setData();

            setMRZData(g_recogResult);

            if (g_recogResult.docFrontBitmap != null) {
                iv_frontside.setImageBitmap(g_recogResult.docFrontBitmap);
            } else {
                ly_front.setVisibility(View.GONE);
            }

            if (g_recogResult.docBackBitmap != null) {
                iv_backside.setImageBitmap(g_recogResult.docBackBitmap);
            } else {
                ly_back.setVisibility(View.GONE);
            }

        }
    }

    private void initUI() {
        //initialize the UI
        ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile2 = findViewById(R.id.ivUserProfile2);
        loutImg = findViewById(R.id.lyt_img_cover);
        loutImg2 = findViewById(R.id.lyt_img_cover2);
        loutLivenessScore = findViewById(R.id.lout_liveness_score);
        loutFmScore = findViewById(R.id.lout_fm_score);
        tvLivenessScore = loutLivenessScore.findViewById(R.id.tv_value);
        tvFaceMatchScore = loutFmScore.findViewById(R.id.tv_value);
        TextView tvLiveness = loutLivenessScore.findViewById(R.id.tv_key);
        TextView tvFaceMatch = loutFmScore.findViewById(R.id.tv_key);
        tvLiveness.setText(getString(R.string.liveness));
        tvFaceMatch.setText(getString(R.string.face_match));

        loutImg2.setVisibility(View.GONE);

        ly_back = findViewById(R.id.ly_back);
        ly_front = findViewById(R.id.ly_front);
        iv_frontside = findViewById(R.id.iv_frontside);
        iv_backside = findViewById(R.id.iv_backside);

        mrz_table_layout = findViewById(R.id.mrz_table_layout);
        front_table_layout = findViewById(R.id.front_table_layout);
        back_table_layout = findViewById(R.id.back_table_layout);

        ly_mrz_container = findViewById(R.id.ly_mrz_container);
        ly_front_container = findViewById(R.id.ly_front_container);
        ly_back_container = findViewById(R.id.ly_back_container);

        loutFmScore.setVisibility(View.GONE);
        loutLivenessScore.setVisibility(View.GONE);
        ly_front_container.setVisibility(View.GONE);
        ly_back_container.setVisibility(View.GONE);
        ly_mrz_container.setVisibility(View.GONE);
    }

    private void setOcrData(OcrData ocrData) {

        Frontdata = ocrData.getFrontData();
        Backdata = ocrData.getBackData();

        if (face1 == null && ocrData.getFaceImage() != null && !ocrData.getFaceImage().isRecycled()) {
            face1 = ocrData.getFaceImage();
        }

        if (Frontdata != null) {
            ly_front_container.setVisibility(View.VISIBLE);
            for (int i = 0; i < Frontdata.getOcr_data().size(); i++) {
                final OcrData.MapData.ScannedData array = Frontdata.getOcr_data().get(i);
                if (array != null) {
                    final int data_type = array.getType();
                    final String key = array.getKey();
                    final String value = array.getKey_data();
                    final View layout = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
                    final TextView tv_key = layout.findViewById(R.id.tv_key);
                    final TextView tv_value = layout.findViewById(R.id.tv_value);
                    final ImageView imageView = layout.findViewById(R.id.iv_image);
                    if (data_type == 1) {
                        if (!key.toLowerCase().contains("mrz")) {
                            if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                                tv_key.setText(key);
                                tv_value.setText(value);
                                imageView.setVisibility(View.GONE);
                                front_table_layout.addView(layout);
                            }
                        } else if (key.toLowerCase().contains("mrz")) {
                            setMRZData(ocrData.getMrzData());
                        }
                    } else if (data_type == 2) {
                        if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                            try {
                                if (key.toLowerCase().contains("face")) {
//                                    if (face1 == null) {
//                                        face1 = array.getImage();
//                                    }
                                } else {
                                    tv_key.setText(key);
                                    Bitmap myBitmap = array.getImage();
                                    if (myBitmap != null) {
                                        imageView.setImageBitmap(myBitmap);
                                        tv_value.setVisibility(View.GONE);
                                        front_table_layout.addView(layout);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            tv_value.setText(value);
                            imageView.setVisibility(View.GONE);
                            front_table_layout.addView(layout);
                        }
                    }
                }
            }
            final Bitmap frontBitmap = ocrData.getFrontimage();
            if (frontBitmap != null && !frontBitmap.isRecycled()) {
                iv_frontside.setImageBitmap(frontBitmap);
            }
        } else {
            ly_front.setVisibility(View.GONE);
            ly_front_container.setVisibility(View.GONE);
        }
        if (Backdata != null) {
            ly_back_container.setVisibility(View.VISIBLE);
            for (int i = 0; i < Backdata.getOcr_data().size(); i++) {
                View layout = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
                TextView tv_key = layout.findViewById(R.id.tv_key);
                TextView tv_value = layout.findViewById(R.id.tv_value);
                ImageView imageView = layout.findViewById(R.id.iv_image);
                final OcrData.MapData.ScannedData array = Backdata.getOcr_data().get(i);

                if (array != null) {
                    int data_type = array.getType();
                    String key = array.getKey();
                    final String value = array.getKey_data();
                    if (data_type == 1) {
                        if (!key.equalsIgnoreCase("mrz")) {
                            if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                                tv_key.setText(key);
                                tv_value.setText(value);
                                imageView.setVisibility(View.GONE);
                                back_table_layout.addView(layout);
                            }
                        } else {
                            setMRZData(ocrData.getMrzData());
                        }
                    } else if (data_type == 2) {
                        if (!value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
                            try {
                                tv_key.setText(key);
                                Bitmap myBitmap = array.getImage();
                                if (myBitmap != null) {
                                    imageView.setImageBitmap(myBitmap);
                                    tv_value.setVisibility(View.GONE);
                                    back_table_layout.addView(layout);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            tv_value.setText(value);
                            imageView.setVisibility(View.GONE);
                            back_table_layout.addView(layout);

                        }
                    }

                }
            }
            final Bitmap BackImage = ocrData.getBackimage();
            if (BackImage != null && !BackImage.isRecycled()) {
                iv_backside.setImageBitmap(BackImage);
            }
        } else {
            ly_back.setVisibility(View.GONE);
            ly_back_container.setVisibility(View.GONE);
        }
        setData();
    }

    private void setMRZData(RecogResult recogResult) {
        if (recogResult == null) {
            return;
        }
        ly_mrz_container.setVisibility(View.VISIBLE);
        addLayout("MRZ", recogResult.lines);
        addLayout("Document Type", recogResult.docType);
        addLayout("First Name", recogResult.givenname);
        addLayout("Last Name", recogResult.surname);
        addLayout("Document No.", recogResult.docnumber);
        addLayout("Document check No.", recogResult.docchecksum);
        addLayout("Correct Document check No.", recogResult.correctdocchecksum);
        addLayout("Country", recogResult.country);
        addLayout("Nationality", recogResult.nationality);
        String s = (recogResult.sex.equals("M")) ? "Male" : ((recogResult.sex.equals("F")) ? "Female" : recogResult.sex);
        addLayout("Sex", s);
        addLayout("Date of Birth", recogResult.birth);
        addLayout("Birth Check No.", recogResult.birthchecksum);
        addLayout("Correct Birth Check No.", recogResult.correctbirthchecksum);
        addLayout("Date of Expiry", recogResult.expirationdate);
        addLayout("Expiration Check No.", recogResult.expirationchecksum);
        addLayout("Correct Expiration Check No.", recogResult.correctexpirationchecksum);
        addLayout("Date Of Issue", recogResult.issuedate);
        addLayout("Department No.", recogResult.departmentnumber);
        addLayout("Other ID", recogResult.otherid);
        addLayout("Other ID Check", recogResult.otheridchecksum);
        addLayout("Correct Other ID Check", recogResult.correctotheridchecksum);
        addLayout("Second Row Check No.", recogResult.secondrowchecksum);
        addLayout("Correct Second Row Check No.", recogResult.correctsecondrowchecksum);
    }

    private void addLayout(String key, String s) {
        if (TextUtils.isEmpty(s)) return;
        View layout1 = LayoutInflater.from(OcrResultActivity.this).inflate(R.layout.table_row, null);
        TextView tv_key1 = layout1.findViewById(R.id.tv_key);
        TextView tv_value1 = layout1.findViewById(R.id.tv_value);
        tv_key1.setText(key);
        tv_value1.setText(s);
        mrz_table_layout.addView(layout1);
    }

    private void setData() {
        if (face1 != null) {
            ivUserProfile.setImageBitmap(face1);
            ivUserProfile.setVisibility(View.VISIBLE);
        } else {
            ivUserProfile.setVisibility(View.GONE);
        }
    }

    public void handleVerificationSuccessResult(final AccuraVerificationResult result) {
        if (result != null) {
//            showProgressDialog();
            Runnable runnable = new Runnable() {
                public void run() {

                    faceHelper.setInputImage(face1);

                    if (result.getLivenessId() == null) {
                        Toast.makeText(OcrResultActivity.this, "Failed to send data on server -> "+result.getApiErrorMessage(), Toast.LENGTH_SHORT).show();
                    }

                    if (result.getFaceBiometrics() != null) {
                        if (result.getLivenessResult() == null) {
                            return;
                        }
                        if (result.getLivenessResult().getLivenessStatus()) {
                            Bitmap face2 = result.getFaceBiometrics();
                            Glide.with(OcrResultActivity.this).load(face2).into(ivUserProfile2);
                            if (face2 != null) {
                                faceHelper.setApiData(Utils.DATABASE_SERVER_URL, Utils.DATABASE_SERVER_KEY, result.getLivenessId());
                                faceHelper.setMatchImage(face2);
                            }
                            setLivenessData(result.getLivenessResult().getLivenessScore() * 100.0 + "");
                        }
                    }


                }
            };
            new Handler().postDelayed(runnable, 100);
        }
    }

    //method for setting liveness data
    //parameter to pass : livenessScore
    private void setLivenessData(String livenessScore) {
        tvLivenessScore.setText(String.format("%s %%", livenessScore.length() > 5 ? livenessScore.substring(0, 5) : livenessScore));
        loutLivenessScore.setVisibility(View.VISIBLE);
        loutFmScore.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACCURA_LIVENESS_CAMERA && data != null) {
                AccuraVerificationResult result = data.getParcelableExtra("Accura.liveness");
                if (result == null) {
                    return;
                }
                if (result.getStatus().equals("1")) {
                    handleVerificationSuccessResult(result);
                } else {
                    Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == 100) {
                if (faceHelper != null && face1 != null) {
                    faceHelper.setInputImage(face1);
                }
                File f = new File(Environment.getExternalStorageDirectory().toString());
                File ttt = null;
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        ttt = temp;
                        break;
                    }
                }
                if (ttt == null)
                    return;

                try {
                    faceHelper.setMatchImage(ttt.getAbsolutePath());
                    ttt.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onCLickFaceMatch(View view) {
        if (view.getId() == R.id.btn_liveness) {
            if (faceHelper == null) {
                faceHelper = new FaceHelper(this);
            } else {
                performClick(false, true);
            }
        }
    }

    private void performClick(boolean isFaceMatch, boolean isLiveness) {
        if (isFaceMatch) openCamera();
        else if (isLiveness) openLivenessCamera();
    }

    private void openLivenessCamera() {
        LivenessCustomization livenessCustomization = new LivenessCustomization();

        livenessCustomization.backGroundColor = getResources().getColor(R.color.livenessBackground);
        livenessCustomization.closeIconColor = getResources().getColor(R.color.livenessCloseIcon);
        livenessCustomization.feedbackBackGroundColor = getResources().getColor(R.color.livenessfeedbackBg);
        livenessCustomization.feedbackTextColor = getResources().getColor(R.color.livenessfeedbackText);
        livenessCustomization.feedbackTextSize = 18;
        livenessCustomization.feedBackframeMessage = "Frame Your Face";
        livenessCustomization.feedBackAwayMessage = "Move Phone Away";
        livenessCustomization.feedBackOpenEyesMessage = "Keep Your Eyes Open";
        livenessCustomization.feedBackCloserMessage = "Move Phone Closer";
        livenessCustomization.feedBackCenterMessage = "Center Your Face";
        livenessCustomization.feedBackMultipleFaceMessage = "Multiple Face Detected";
        livenessCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
        livenessCustomization.feedBackLowLightMessage = "Low light detected";
        livenessCustomization.feedBackBlurFaceMessage = "Blur Detected Over Face";
        livenessCustomization.feedBackGlareFaceMessage = "Glare Detected";

        livenessCustomization.setLowLightTolerence(39);
        livenessCustomization.setBlurPercentage(80);
        livenessCustomization.setGlarePercentage(-1, -1);

        Intent intent = SelfieCameraActivity.getCustomIntent(this, livenessCustomization, "your_liveness_url",Utils.DATABASE_SERVER_URL, Utils.DATABASE_SERVER_KEY, kycId);
        startActivityForResult(intent, ACCURA_LIVENESS_CAMERA);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
        Uri uriForFile = FileProvider.getUriForFile(
                OcrResultActivity.this,
                getPackageName() + ".provider",
                f
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        } else {
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
        startActivityForResult(intent, 100);

    }

    @Override
    public void onFaceMatch(float score) {
        tvFaceMatchScore.setText(String.format("%.2f %%", score));
        loutLivenessScore.setVisibility(View.VISIBLE);
        loutFmScore.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSetInputImage(Bitmap bitmap) {

    }

    @Override
    public void onSetMatchImage(Bitmap bitmap) {

    }

    @Override
    public void onInitEngine(int i) {
        if (i != -1) {
            performClick(false, true);
        }
    }

    @Override
    public void onLeftDetect(FaceDetectionResult faceDetectionResult) {
        faceHelper.onLeftDetect(faceDetectionResult);
    }

    @Override
    public void onRightDetect(FaceDetectionResult faceDetectionResult) {
        if (faceDetectionResult != null) {
            try {
                Bitmap bitmap = BitmapHelper.createFromARGB(faceDetectionResult.getNewImg(), faceDetectionResult.getNewWidth(), faceDetectionResult.getNewHeight());
                face2 = faceDetectionResult.getFaceImage(bitmap);
                Glide.with(this).load(face2).into(ivUserProfile2);
                loutImg2.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        faceHelper.onRightDetect(faceDetectionResult);
    }

    @Override
    public void onExtractInit(int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {

        //<editor-fold desc="To resolve memory leak">
        if ((RecogType.detachFrom(getIntent()) == RecogType.OCR) && OcrData.getOcrResult() != null) {
            try {
                OcrData.getOcrResult().getFrontimage().recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                OcrData.getOcrResult().getBackimage().recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                OcrData.getOcrResult().getFaceImage().recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (RecogType.detachFrom(getIntent()) == RecogType.MRZ && RecogResult.getRecogResult() != null) {
            try {
                RecogResult.getRecogResult().docFrontBitmap.recycle();
                RecogResult.getRecogResult().faceBitmap.recycle();
                RecogResult.getRecogResult().docBackBitmap.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //</editor-fold>

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setResult(RESULT_OK);
        finish();
    }

}
