# Accura Qatar Android SDK - OCR, Face Match & Liveness Check
Android Qatar SDK - OCR &amp; Face Match <br/>
Accura OCR is used for Optical character recognition.<br/>

Below steps to setup Accura SDK's to your project.

## Install Qatar SDK in to your App

#### Step 1: Add the JitPack repository to your build file:
    Add it in your root build.gradle at the end of repositories.

    allprojects {
        repositories {
            ...
            maven {
                url 'https://jitpack.io'
                credentials { username authToken }
            }
        }
    }

#### Step 2. Add the token to `gradle.properties`:

    authToken=jp_ssguccab6c5ge2l4jitaj92ek2

#### Step 3: Add the dependency:<br />
```
    Set AccuraOcr as a dependency to our app level `build.gradle` file.

    android {
        defaultConfig {
            ...
            ndk {
                // Specify CPU architecture.
                // 'armeabi-v7a' & 'arm64-v8a' are respectively 32 bit and 64 bit device architecture 
                // 'x86' & 'x86_64' are respectively 32 bit and 64 bit emulator architecture
                abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
            }
        }
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_17
            targetCompatibility JavaVersion.VERSION_17
        }
        packagingOptions {
            pickFirst 'lib/arm64-v8a/libcrypto.so'
            pickFirst 'lib/arm64-v8a/libssl.so'

            pickFirst 'lib/armeabi-v7a/libcrypto.so'
            pickFirst 'lib/armeabi-v7a/libssl.so'

            pickFirst 'lib/x86/libcrypto.so'
            pickFirst 'lib/x86/libssl.so'

            pickFirst 'lib/x86_64/libcrypto.so'
            pickFirst 'lib/x86_64/libssl.so'
        }
    }
    dependencies {
        ...
        // for Accura qatar ocr
        implementation 'com.github.accurascan:Qatar-SDK-Android:2.5.3'
        // for liveness
        implementation 'com.github.accurascan:Qatar-Liveness-Android:2.3.1'
        // for Accura Face Match
        implementation 'com.github.accurascan:Qatar-FaceMatch-Android:2.2.3'
    }
```
#### Step 4: Add files to project assets folder:<br />
```
    Create assets folder under app/src/main and add license file in to assets folder.<br />
    - key.license // for Accura Qatar <br />
    - accuraface.license // for Accura Face Match <br />
    Generate your Accura license from https://accurascan.com/developer/dashboard
```

## 1. Setup Accura Qatar OCR

#### Step 1 : To initialize sdk on app start:
```
    RecogEngine recogEngine = new RecogEngine();
    RecogEngine.SDKModel sdkModel = recogEngine.initEngine(your activity context);

    if (sdkModel.i > 0) { // means license is valid

         if (sdkModel.isMRZEnable) // RecogType.MRZ

        // if sdkModel.isOCREnable true then get list of the cards which you have added while creating license
        if (sdkModel.isOCREnable) List<ContryModel> modelList = recogEngine.getCardList(MainActivity.this);
        if (modelList != null) { // if country & card added in license

            Use following code to get country card details.
            ContryModel contryModel = modelList.get(/*selected country position*/);
            contryModel.getCountry_id(); // getting country id
            CardModel model = contryModel.getCards().get(/*selected card position*/); // getting card
            model.getCard_id() // getting card id
            model.getCard_name()  // getting card name
        }
    }
```
##### Update filters like below.</br>
  Call this function after initialize sdk if license is valid(sdkModel.i > 0)
   * Set Blur Percentage to allow blur on document

        ```
		//0 for clean document and 100 for Blurry document
		recogEngine.setBlurPercentage(Context context, int /*blurPercentage*/50);
		```
   * Set Face blur Percentage to allow blur on detected Face

        ```
		// 0 for clean face and 100 for Blurry face
		recogEngine.setFaceBlurPercentage(Context context, int /*faceBlurPercentage*/50);
        ```
   * Set Glare Percentage to detect Glare on document

        ```
		// Set min and max percentage for glare
		recogEngine.setGlarePercentage(Context context, int /*minPercentage*/6, int /*maxPercentage*/98);
		```
   * To remove extra brightness from the document

        ```
   		// Set true to enable and set the threshold above which it will remove extra brightness
   		//Threshold is the brightness value of document , above which it will remove extra brightness of document
   		recogEngine.doRemoveBrightness(Context context,boolean /*doRemoveBrightness*/ true ,int /*threshold*/ 200 or more);
   		```


   * Set Photo Copy to allow photocopy document or not

        ```
		// Set min and max percentage for glare
		recogEngine.isCheckPhotoCopy(Context context, boolean /*isCheckPhotoCopy*/false);
		```
   * Set Hologram detection to verify the hologram on the face

        ```
		// true to check hologram on face
		recogEngine.SetHologramDetection(Context context, boolean /*isDetectHologram*/true);
		```
   * Set light tolerance to detect light on document

        ```
        // 0 for full dark document and 100 for full bright document
        recogEngine.setLowLightTolerance(Context context, int /*tolerance*/30);
        ```
   * Set motion threshold to detect motion on camera document
		```
        // 1 - allows 1% motion on document and
        // 100 - it can not detect motion and allow document to scan.
        recogEngine.setMotionThreshold(Context context, int /*motionThreshold*/18);
        ```

#### Step 2 : Set CameraView

    /*
    * Must have to extend com.accurascan.libqatar.motiondetection.SensorsActivity to your activity.
    */

    private CameraView cameraView;
    private int cardId,countryId;
    private String cardName;
    private RecogType recogType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeNoActionBar); // To hide toolbar
        setContentView(R.layout.your_layout);

        // Recog type selection base on your license data
        // As like RecogType.OCR, RecogType.MRZ

        recogType = RecogType.OCR;

        //Below code it's only for Qatar National Id card(for RecogType.OCR) and for RecogType.MRZ no need of below code.
        countryId = ContryModel.getCountry_id();
        CardModel model = ContryModel.getCards().get(/*selected card position*/);
        cardId = model.getCard_id() // getting card id
        cardName = model.getCard_name()  // getting card name

        // initialized camera
        initCamera();
    }

    private void initCamera() {
        //<editor-fold desc="To get status bar height">
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarTop = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int statusBarHeight = contentViewTop - statusBarTop;
        //</editor-fold>

        RelativeLayout cameraContainer = findViewById(R.id.ocr_root); // layout width and height is match_parent

        cameraView = new CameraView(this);
        if (recogType == RecogType.OCR) {
            // must have to set data for RecogType.OCR
            cameraView.setCountryId(countryId).setCardId(cardId);
        }
        cameraView.setRecogType(recogType) // is for qatar Id card or MRZ document
                .setMinFrameForValidate(3) // To add frame count for DOB and DOE. Values can be any positive integer
                .setMinFrameQatarName_IDNo_Validate(3) // To add frame count for Name and ID number. Values can be any positive integer
                .setView(cameraContainer) // To add camera view
                .setOcrCallback(this)  // To get Update and Success Callback
                .setStatusBarHeight(statusBarHeight)  // To remove Height from Camera View if status bar visible
                .setAPIData("your server url", "your server api key") // to send data on server
	//                Optional field
	//                .setEnableMediaPlayer(false) // false to disable sound and true to enable sound. Default it's true
	//                .setCustomMediaPlayer(MediaPlayer.create(this, com.accurascan.ocr.mrz.R.raw.beep)) // To add custom sound make sure media player is enable.
                .init();  // initialized camera
    }

    /**
     * To handle camera on window focus update
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (cameraView != null) {
            cameraView.onWindowFocusUpdate(hasFocus);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        cameraView.onDestroy();
        super.onDestroy();
    }

    /**
     * To update your border frame according to width and height
     * it's different for different card
     * call {@link CameraView#startOcrScan(false)} method to start camera preview
     * @param width    border layout width
     * @param height   border layout height
     */

    @Override
    public void onUpdateLayout(int width, int height) {
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
        // display data on ui thread
        Log.e("TAG", "onScannedComplete: ");
        if (result != null) {

            // Do some code for display data

            // Use startActivityForResult(Intent, RESULT_ACTIVITY_CODE) to restart scanning after come back from result activity

            if (result instanceof OcrData) {
                // @recogType is {@see com.docrecog.scan.RecogType#OCR}
                OcrData.setOcrResult((OcrData) result); // Set data To retrieve it anywhere
            } else if (result instanceof RecogResult) {
                // @recogType is {@see com.docrecog.scan.RecogType#MRZ}
                RecogResult.setRecogResult((RecogResult) result); // Set data To retrieve it anywhere
            }

        } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param titleCode to display scan card message on top of border Frame
     *
     * @param errorMessage To display process message.
     *                null if message is not available
     * @param isFlip  if true then set your customize animation for scan back card after complete front scan
     *                and also used cameraView.flipImage(ImageView) for default animation
     */
    @Override
    public void onProcessUpdate(int titleCode, String message, boolean isFlip) {
        // make sure update view on ui thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getTitleMessage(titleCode) != null) { // check
                    Toast.makeText(this, getTitleMessage(titleCode), Toast.LENGTH_SHORT).show(); // display title
                }
                if (message != null) {
                    Toast.makeText(this, getErrorMessage(message), Toast.LENGTH_SHORT).show(); // display message
                }
                if (isFlip) {
                // To set default animation or remove this line to set your custom animation after successfully scan front side.
                    cameraView.flipImage(imageFlip);
                }
            }
        });
    }

    /**
     * To know recognize process while scanning card
     *
     * @param requiredFields  An array of required fields to scanned card
     * @param recognizedFields An array of scanned data from card
     */
    @Override
    public void onRecognizeProcess(String[] requiredFields, String[] recognizedFields) {
        ...
    }

    @Override
    public void onError(String errorMessage) {
        // make sure update view on ui thread
        // stop ocr if failed
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onAPIError(String errorMessage) {
        Runnable runnable = () -> Toast.makeText(OcrActivity.this, "Failed to send data on server -> " + errorMessage, Toast.LENGTH_LONG).show();
        runOnUiThread(runnable);
    }

    // Customize messages in below function as per requirement
    private String getTitleMessage(int titleCode) {
        if (titleCode < 0) return null;
        switch (titleCode){
            case RecogEngine.SCAN_TITLE_OCR_FRONT:// for front side ocr;
                return String.format("Scan Front Side of %s", cardName);
            case RecogEngine.SCAN_TITLE_OCR_BACK: // for back side ocr
                return String.format("Scan Back Side of %s", cardName);
            case RecogEngine.SCAN_TITLE_MRZ:// for front side MRZ
                return "Scan Front Side of Document";
            default:return "";
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

    // After getting result to restart scanning you have to set below code onActivityResult
    // when you use startActivityForResult(Intent, RESULT_ACTIVITY_CODE) to open result activity.
	@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_ACTIVITY_CODE) {
                Runtime.getRuntime().gc(); // To clear garbage
                //<editor-fold desc="Call CameraView#startOcrScan(true) To start again Camera Preview
                //to Rescan card And CameraView#startOcrScan(false) To start first time">

                if (cameraView != null) cameraView.startOcrScan(true);

                //</editor-fold>
            }
        }
    }

## 2. Setup Accura Liveness

    Contact to connect@accurascan.com to get Url for liveness

#### Step 1 : Add following code in Manifest.

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />

#### Step 2 : Open camera for liveness Detectcion.

    Must to Grant camera permission

    private final static int ACCURA_LIVENESS_CAMERA = 100;

    // To customize your screen theme and feed back messages
    LivenessCustomization livenessCustomization = new LivenessCustomization();

    livenessCustomization.backGroundColor = getResources().getColor(R.color.livenessBackground);
    livenessCustomization.closeIconColor = getResources().getColor(R.color.livenessCloseIcon);
    livenessCustomization.feedbackBackGroundColor = Color.TRANSPARENT;
    livenessCustomization.feedbackTextColor = Color.BLACK;
    livenessCustomization.feedbackTextSize = 18;
    livenessCustomization.feedBackframeMessage = "Frame Your Face";
    livenessCustomization.feedBackAwayMessage = "Move Phone Away";
    livenessCustomization.feedBackOpenEyesMessage = "Keep Open Your Eyes";
    livenessCustomization.feedBackCloserMessage = "Move Phone Closer";
    livenessCustomization.feedBackCenterMessage = "Center Your Face";
    livenessCustomization.feedBackMultipleFaceMessage = "Multiple Face Detected";
    livenessCustomization.feedBackHeadStraightMessage = "Keep Your Head Straight";
    livenessCustomization.feedBackLowLightMessage = "Low light detected";
    livenessCustomization.feedBackBlurFaceMessage = "Blur Detected Over Face";
    livenessCustomization.feedBackGlareFaceMessage = "Glare Detected";
    
    // 0 is allow dark face and 100 is allow bright face or -1 to remove low light filter
    livenessCustomization.setLowLightTolerence(31);
    
    // 0 for clean face and 100 for Blurry face or -1 to remove blur filter
    livenessCustomization.setBlurPercentage(75/*blurPercentage*/); // To allow blur on face
    
    // Set min and max percentage for glare or set it -1 to remove glare filter
    livenessCustomization.setGlarePercentage(6/*glareMinPercentage*/, 99/*glareMaxPercentage*/);

    // must have to call SelfieCameraActivity.getCustomIntent() to create intent
    // kycId is OcrData.getKycId() for qatar id card and RecogResult.kycId for passport MRZ document
    Intent intent = SelfieCameraActivity.getCustomIntent(this, livenessCustomization, "your_liveness_url","your server url", "your server api key", kycId);
    startActivityForResult(intent, ACCURA_LIVENESS_CAMERA);


    // Handle accura liveness result.
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
                    // result get bitmap of face by using following code
                    Bitmap bitmap = result.getFaceBiometrics();
                    double livenessScore = result.getLivenessResult().getLivenessScore() * 100.0;
                    Toast.makeText(this, "Liveness Score : " + livenessScore, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, result.getStatus() + " " + result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                if (result.getLivenessId() == null) {
                    Toast.makeText(OcrResultActivity.this, "Failed to send data on server -> "+result.getApiErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

## 3. Setup Accura Face Match

#### Step 1 : Simple Usage to face match in your app.

    // Just add FaceMatch activity to your manifest:
    <activity android:name="com.accurascan.facematch.ui.FaceMatchActivity"/>

    // Start Intent to open activity
    Intent intent = new Intent(this, FaceMatchActivity.class);
    startActivity(intent);

#### Step 2 : Implement face match code manually to your activity.

    Important Grant Camera and storage Permission.

    must have to implements FaceCallback, FaceHelper.FaceMatchCallBack to your activity
    ImageView image1,image2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.your_layout);
        // Initialized facehelper in onCreate.
        FaceHelper helper = new FaceHelper(this);

        TextView tvFaceMatch = findViewById(R.id.tvFM);
        tvFaceMatch.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ********** For faceMatch

                // To pass two image uri for facematch.
                // @params uri1 is for input image
                // @params uri2 is for match image

                helper.getFaceMatchScore(uri1, uri2);


                // also use some other method for for face match
                // must have to helper.setInputImage first and then helper.setMatchImage
                // helper.setInputImage(uri1);
                // FaceHelper.setApiData("your server url", "your server api key", AccuraVerificationResult.getLivenessId()) 
                // helper.setMatchImage(uri2);

            }
        });
    }

    // Override methods of FaceMatchCallBack
    @Override
    public void onFaceMatch(float score) {
        // get face match score
        System.out.println("Match Score : " + ss + " %");
    }

    @Override
    public void onSetInputImage(Bitmap src1) {
        // set Input image to your view
        image1.setImageBitmap(src1);
    }

    @Override
    public void onSetMatchImage(Bitmap src2) {
        // set Match image to your view
        image2.setImageBitmap(src2);
    }

    // Override methods for FaceCallback

    @Override
    public void onInitEngine(int ret) {
    }

    //call if face detect

    @Override
    public void onLeftDetect(FaceDetectionResult faceResult) {
        // must have to call helper method onLeftDetect(faceResult) to get faceMatch score.
        helper.onLeftDetect(faceResult);
    }

    //call if face detect
    @Override
    public void onRightDetect(FaceDetectionResult faceResult) {
        // must have to call helper method onRightDetect(faceResult) to get faceMatch score.
        helper.onRightDetect(faceResult);
    }

    @Override
    public void onExtractInit(int ret) {
    }

## ProGuard
Depending on your ProGuard (DexGuard) config and usage, you may need to include the following lines in your proguards.
```
-keep public class com.docrecog.scan.ImageOpencv {*;}
-keep class com.accurascan.libqatar.model.** {*;}
-keep class com.accurascan.libqatar.interfaces.** {*;}
-keep public class com.inet.facelock.callback.FaceCallback {*;}
-keep public class com.inet.facelock.callback.FaceDetectionResult {*;}
```
