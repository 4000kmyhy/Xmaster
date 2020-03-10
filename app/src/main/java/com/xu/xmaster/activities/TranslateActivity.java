package com.xu.xmaster.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;
import com.xu.xmaster.Constant;
import com.xu.xmaster.R;
import com.xu.xmaster.adapters.TransRecordAdapter;
import com.xu.xmaster.base.BaseActivity;
import com.xu.xmaster.database.TransRecordDBHelper;
import com.xu.xmaster.utils.DecodeUtils;
import com.xu.xmaster.utils.JsonParser;
import com.xu.xmaster.utils.MD5Utils;
import com.xu.xmaster.utils.RowLayoutManager;
import com.xu.xmaster.utils.net.INetCallBack;
import com.xu.xmaster.utils.net.OkHttpUtil;
import com.xu.xmaster.views.SimpleToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TranslateActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "TranslateActivity";

    private SimpleToolbar toolbar;
    private TextView tv_from, tv_to, btn_trans, tv_output, btn_clear, btn_del, btn_done;
    private ImageView iv_clear, iv_input_volume, iv_output_volume;
    private EditText et_input;
    private RecyclerView rv_history;
    private LinearLayout layout_edit;
    private Dialog mDialog;
    private WheelView wv_from, wv_to;

    private String[] baiduLanEntries, baiduLanValue, iatLanEntries, iatLanValue;

    private int from_position = 0, to_position = 0;

    private TransRecordAdapter mAdapter;
    private List<String> mList;
    private TransRecordDBHelper helper;

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 语音合成对象
    private SpeechSynthesizer mTts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_translate);
        SpeechUtility.createUtility(getContext(), SpeechConstant.APPID + "=" + Constant.iFLYTEK_appid);

        baiduLanEntries = getResources().getStringArray(R.array.baidu_language_entries);
        baiduLanValue = getResources().getStringArray(R.array.baidu_language_value);
        iatLanEntries = getResources().getStringArray(R.array.iat_language_entries);
        iatLanValue = getResources().getStringArray(R.array.iat_language_value);

        initView();
        initEvent();
    }

    @SuppressLint("CheckResult")
    private void initEvent() {
        toolbar.setLeftBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbar.setRightBtn1OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIatResults.clear();
                setIatParam(tv_from.getText().toString());
                // 显示听写对话框
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
                showToast("请开始说话");
            }
        });

        tv_from.setOnClickListener(this);
        tv_to.setOnClickListener(this);
        btn_trans.setOnClickListener(this);
        iv_clear.setOnClickListener(this);
        iv_input_volume.setOnClickListener(this);
        iv_output_volume.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btn_del.setOnClickListener(this);
        btn_done.setOnClickListener(this);

        RxTextView.textChanges(et_input)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                })
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (TextUtils.isEmpty(s)) {
                            iv_clear.setVisibility(View.GONE);
                            iv_input_volume.setVisibility(View.GONE);
                        } else {
                            iv_clear.setVisibility(View.VISIBLE);
                            iv_input_volume.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        getTranslate(s);
                    }
                });

        RxTextView.textChanges(tv_output)
                .map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (TextUtils.isEmpty(s)) {
                            iv_output_volume.setVisibility(View.GONE);
                        } else {
                            iv_output_volume.setVisibility(View.VISIBLE);
                        }
                    }
                });

        mAdapter.setOnItemClickListener(new TransRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position, boolean isShowDel) {
                if (!isShowDel) {
                    et_input.setText(mAdapter.getItemObject(position));
                    et_input.setSelection(et_input.getText().toString().length());
                } else {
                    helper.deleteData(mAdapter.getItemObject(position));
                    queryData();
                }
            }
        });
    }

    private void initView() {
        //ui
        toolbar = findViewById(R.id.toolbar);
        tv_from = findViewById(R.id.tv_from);
        tv_to = findViewById(R.id.tv_to);
        btn_trans = findViewById(R.id.btn_trans);
        et_input = findViewById(R.id.et_input);
        tv_output = findViewById(R.id.tv_output);
        layout_edit = findViewById(R.id.layout_edit);
        btn_clear = findViewById(R.id.btn_clear);
        btn_del = findViewById(R.id.btn_del);
        btn_done = findViewById(R.id.btn_done);
        rv_history = findViewById(R.id.rv_history);
        iv_clear = findViewById(R.id.iv_clear);
        iv_input_volume = findViewById(R.id.iv_input_volume);
        iv_output_volume = findViewById(R.id.iv_output_volume);
        initDialog();
        toolbar.setPaddingTop();

        //数据库
        helper = new TransRecordDBHelper(getContext());

        //历史记录表
        mList = new ArrayList<>();
        mAdapter = new TransRecordAdapter(getContext(), mList);
        RowLayoutManager layoutManager = new RowLayoutManager();
        layoutManager.setAutoMeasureEnabled(true);
        rv_history.setLayoutManager(layoutManager);
        rv_history.setAdapter(mAdapter);
        rv_history.setNestedScrollingEnabled(false);
        queryData();

        //语音转文字、文字转语音
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(getContext(), mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(getContext(), mInitListener);
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(getContext(), mInitListener);

        mSharedPreferences = getSharedPreferences("com.iflytek.setting",
                Activity.MODE_PRIVATE);
    }

    private void startAnimation(ImageView iv) {
        AnimationDrawable animDrawable = new AnimationDrawable();
        animDrawable.addFrame(getDrawable(R.drawable.ic_volume2), 200);
        animDrawable.addFrame(getDrawable(R.drawable.ic_volume1), 200);
        animDrawable.addFrame(getDrawable(R.drawable.ic_volume0), 200);
        animDrawable.addFrame(getDrawable(R.drawable.ic_volume1), 200);
        iv.setImageDrawable(animDrawable);
        animDrawable.start();
    }

    private void initDialog() {
        mDialog = new Dialog(this, R.style.bottom_dialog);
        mDialog.setCanceledOnTouchOutside(true);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_translate, null, false);
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lps = window.getAttributes();
        lps.width = WindowManager.LayoutParams.MATCH_PARENT;
        lps.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lps);

        //UI
        TextView btnLeft = view.findViewById(R.id.dialog_left);
        TextView btnRight = view.findViewById(R.id.dialog_right);
        wv_from = view.findViewById(R.id.wheelView_from);
        wv_to = view.findViewById(R.id.wheelView_to);

        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.selectedTextSize = 18;
        style.selectedTextColor = Color.parseColor("#318DF3");
        style.textSize = 14;

        wv_from.setWheelAdapter(new ArrayWheelAdapter(this));
        wv_from.setSkin(WheelView.Skin.Holo);
        wv_from.setWheelData(Arrays.asList(baiduLanEntries));
        wv_from.setStyle(style);
        wv_from.setWheelSize(5);

        wv_to.setWheelAdapter(new ArrayWheelAdapter(this));
        wv_to.setSkin(WheelView.Skin.Holo);
        wv_to.setWheelData(Arrays.asList(baiduLanEntries));
        wv_to.setStyle(style);
        wv_to.setWheelSize(5);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from_position = wv_from.getCurrentPosition();
                tv_from.setText(baiduLanEntries[from_position]);
                to_position = wv_to.getCurrentPosition();
                tv_to.setText(baiduLanEntries[to_position]);
                getTranslate(et_input.getText().toString());
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(view);
    }

    private void getTranslate(String q) {
        if (TextUtils.isEmpty(q)) {
            tv_output.setText("");
            return;
        }
        helper.insertData(q);
        queryData();

        String from = baiduLanValue[from_position];
        String to = baiduLanValue[to_position];
        String appid = Constant.translate_appid;
        long salt = new Date().getTime();
        String key = Constant.translate_key;
        String sign = MD5Utils.getMessageDigest(appid + q + salt + key);
        String url = Constant.translateAPI +
                "?q=" + DecodeUtils.toURLEncoded(q) +
                "&from=" + from +
                "&to=" + to +
                "&appid=" + appid +
                "&salt=" + salt +
                "&sign=" + sign;
        Log.d(TAG, "initData: url=" + url);
        OkHttpUtil.getInstance().getNetManager().get(url, new INetCallBack() {
            @Override
            public void success(String response) {
                Log.d(TAG, "initData: success=" + response);
                parseJson(response);
            }

            @Override
            public void failed(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private void parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String from = jsonObject.optString("from", "auto");
            String to = jsonObject.optString("to", "auto");
            JSONArray result = jsonObject.getJSONArray("trans_result");
            for (int i = 0; i < baiduLanValue.length; i++) {
                if (TextUtils.equals(from, baiduLanValue[i])) {
                    from_position = i;
                    tv_from.setText(baiduLanEntries[from_position]);
                }
            }
            for (int i = 0; i < baiduLanValue.length; i++) {
                if (TextUtils.equals(to, baiduLanValue[i])) {
                    to_position = i;
                    tv_to.setText(baiduLanEntries[to_position]);
                }
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length(); i++) {
                JSONObject temp = result.getJSONObject(i);
                String src = temp.optString("src", "");
                String dst = temp.optString("dst", "");
                if (i != 0) sb.append("\n");
                sb.append(DecodeUtils.toURLDecoded(dst));
            }
            tv_output.setText(sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void queryData() {
        mList.clear();
        mList.addAll(helper.queryData());
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showToast("初始化失败：" + code);
            }
        }
    };

    /**
     * 参数设置
     */
    public void setIatParam(String language) {
        //默认语言
        String languageValue = "zh_cn";
        for (int i = 0; i < iatLanEntries.length; i++) {
            if (TextUtils.equals(language, iatLanEntries[i])) {
                languageValue = iatLanValue[i];
                break;
            }
        }
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        if (languageValue.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");//普通话
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, languageValue);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showToast(error.getPlainDescription(true));
        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        et_input.setText(resultBuffer.toString());
        et_input.setSelection(et_input.length());
    }

    /**
     * 参数设置
     */
    private void setTtsParam(String language) {
        // 默认发音人
        String voicer = "xiaoyan";
        switch (language) {
            case "粤语":
                voicer = "xiaomei";
                break;
            case "中文":
                voicer = "nannan";
                break;
            case "英语":
            default:
                voicer = "xiaoyan";
                break;
        }
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //支持实时音频返回，仅在synthesizeToUri条件下支持
        mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
        //	mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME,"1");
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.pcm");
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showToast("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showToast("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showToast("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
            Log.d(TAG, "percent1=" + percent + info);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            Log.d(TAG, "percent2=" + percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            iv_input_volume.setImageResource(R.drawable.ic_volume2);
            iv_output_volume.setImageResource(R.drawable.ic_volume2);
            showToast("播放完成");
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            //	 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
//            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
//                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
//                Log.d(TAG, "session id =" + sid);
//            }
            Log.d(TAG, "onEvent: " + eventType);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_from:
            case R.id.tv_to:
                wv_from.setSelection(from_position);
                wv_to.setSelection(to_position);
                mDialog.show();
                break;
            case R.id.btn_trans:
                getTranslate(et_input.getText().toString());
                break;
            case R.id.iv_clear:
                et_input.setText("");
                break;
            case R.id.btn_clear:
                helper.clearData();
                queryData();
                break;
            case R.id.btn_del:
                mAdapter.setShowDel(true);
                layout_edit.setVisibility(View.GONE);
                btn_done.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_done:
                mAdapter.setShowDel(false);
                layout_edit.setVisibility(View.VISIBLE);
                btn_done.setVisibility(View.GONE);
                break;
            case R.id.iv_input_volume:
                iv_output_volume.setImageResource(R.drawable.ic_volume2);
                startAnimation(iv_input_volume);
                // 设置参数
                setTtsParam(tv_from.getText().toString());
                mTts.startSpeaking(et_input.getText().toString(), mTtsListener);
                break;
            case R.id.iv_output_volume:
                iv_input_volume.setImageResource(R.drawable.ic_volume2);
                startAnimation(iv_output_volume);
                // 设置参数
                setTtsParam(tv_to.getText().toString());
                if (TextUtils.equals(tv_to.getText().toString(), "粤语")) {//如果是粤语，要读翻译前的文字
                    mTts.startSpeaking(et_input.getText().toString(), mTtsListener);
                } else {
                    mTts.startSpeaking(tv_output.getText().toString(), mTtsListener);
                }
                break;
        }
    }
}
