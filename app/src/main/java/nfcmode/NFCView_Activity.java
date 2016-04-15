package nfcmode;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.charset.Charset;

import mainview.demo.DemoApplication;
import mainview.demo.R;

/**
 * 读取NFC标签时显示界面
 */
public class NFCView_Activity extends Activity {
    private int count = 0;// 计数器
    private int all_count = 7;// 计数总数

    private int[] imageint = {R.drawable.nfcview1, R.drawable.nfcview2,
            R.drawable.nfcview3, R.drawable.nfcview4, R.drawable.nfcview5,
            R.drawable.nfcview6, R.drawable.nfcview7};
    private final int STEP_CAHNGE = 0;// 切换消息
    private final int END_CAHNGE = 1;// 切换消息

    // 标签信息
    private NfcAdapter myAdapter;// NFC适配器
    private NdefMessage[] messages;

    //用于声明想要拦截的Intent的Intent Filter数组
    private IntentFilter[] intentFiltersArray;
    //用于打包Tag Intent的Intent
    private PendingIntent pendingIntent;
    //想要处理的标签技术的数组
    private String[][] techListArray;

    // 读取标签信息
    private String uid_string;
    private String tag_string;
    private DemoApplication myApplication;
    changeThread cthread = new changeThread();

    private ImageView nfcview = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nfcview_layout);
        myApplication = (DemoApplication) super.getApplication();

        nfcview = (ImageView) super.findViewById(R.id.nfcview);
        count = 0;
        initnfc();
        cthread.start();
    }

    // ================ NFC前台调度机制 ============== //
    private void initnfc() {
        if (myAdapter != null) {
            // 用来封装NFC标签的详细信息
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            // 表示支持的相关NFC技术的类
            techListArray = new String[][]{new String[]{NfcV.class.getName()},
                    new String[]{MifareClassic.class.getName(), NfcA.class.getName()}};
            // 生命Intent Filter
            IntentFilter ndefFilter = new IntentFilter("android.nfc.action.NDEF_DISCOVERED");
            IntentFilter tagFilter = new IntentFilter("android.nfc.action.TAG_DISCOVERED");
            IntentFilter techFilter = new IntentFilter("android.nfc.action.Tech_DISCOVERED");
            try {
                ndefFilter.addDataType("*/*");
            } catch (MalformedMimeTypeException e) {
                throw new RuntimeException("fail", e);
            }
            intentFiltersArray = new IntentFilter[]{ndefFilter, techFilter, tagFilter};
        } else {
            myAdapter = NfcAdapter.getDefaultAdapter(this);
        }
    }

    public void onPause() {
        super.onPause();
        //禁用前台分派系统
        if (myAdapter != null)
            myAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        //开启前台分派系统
        if (myAdapter != null)
            myAdapter.enableForegroundDispatch(this, pendingIntent,
                    intentFiltersArray, techListArray);
    }

    public void onNewIntent(Intent intent) {
        // 处理接收到的数据信息
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            messages = getNdefMessage(intent);
            parseMessage(messages);
            Toast.makeText(this, parseMessage(messages), Toast.LENGTH_SHORT).show();
            resolveIntent(getIntent());
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String showString = "支持的tech格式为：\n";
            for (int i = 0; i < techList.length; i++) {
                showString += (i + 1) + "、" + techList[i] + "\n";
            }
            Toast.makeText(this, showString, Toast.LENGTH_SHORT).show();
            resolveIntent(getIntent());
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.i("LOG_TAG", "无法解读的tech标签");
        } else {
            Toast.makeText(this, "无法识别的标签", Toast.LENGTH_SHORT).show();
        }
    }

    public void showResult(View source) {
        Intent phoneIntent = new Intent();
        phoneIntent.setAction("WRITEOPTION");
        startActivity(phoneIntent);
    }

    public NdefMessage[] getNdefMessage(Intent intent) {
        NdefMessage[] message = null;
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                message = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    message[i] = (NdefMessage) rawMessages[i];
                }
            } else {
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                message = new NdefMessage[]{msg};
            }

        } else {
            Log.d("", "Unknown intent");
            finish();
        }
        return message;
    }

    public StringBuffer parseMessage(NdefMessage[] messages) {
        StringBuffer myText = new StringBuffer();
        String payload = null;
        StringBuffer toast = new StringBuffer();
        toast.append("created");
        try {
            if ((getIntent().getScheme() != null)
                    && getIntent().getScheme().equalsIgnoreCase("tel"))
                for (int i = 0; i < messages.length; i++) {
                    for (int j = 0; j < messages[0].getRecords().length; j++) {
                        NdefRecord record = messages[i].getRecords()[j];
                        payload = new String(record.getPayload(), 1, record.getPayload().length - 1, Charset.forName("UTF-8"));

                    }
                }
            else if ((getIntent().getType() != null)
                    && getIntent().getType().equalsIgnoreCase("text/plain")) {
                for (int i = 0; i < messages.length; i++) {
                    for (int j = 0; j < messages[0].getRecords().length; j++) {

                        NdefRecord record = messages[i].getRecords()[j];
                        toast.append("recod");
                        byte statusByte = record.getPayload()[0];
                        toast.append("statusByte");
                        int languageCodeLength = statusByte & 0x3F;
                        toast.append("languageCodeLength");
                        int isUTF8 = statusByte - languageCodeLength;
                        if (isUTF8 == 0x00) {
                            payload = new String(record.getPayload(), 1 + languageCodeLength,
                                    record.getPayload().length - 1 - languageCodeLength, Charset.forName("UTF-8"));
                        } else if (isUTF8 == -0x80) {
                            payload = new String(record.getPayload(), 1 + languageCodeLength,
                                    record.getPayload().length - 1 - languageCodeLength, Charset.forName("UTF-16"));
                        } else {

                        }
                        tag_string = payload;
                        Log.e("tag", tag_string);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e + getIntent().getType() + "	" + toast, Toast.LENGTH_LONG).show();
        }
        return myText;
    }

    /**
     * 通过Intent解析获得标签的UID
     */
    void resolveIntent(Intent intent) {
        uid_string = Coverter.getUid(intent);
        Log.e("uid", uid_string);
    }

    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEP_CAHNGE:
                    nfcview.setBackgroundResource(imageint[count]);
                    count++;
                    break;
                case END_CAHNGE:
                    NFC_Reader nfc_reader = new NFC_Reader(myApplication, uid_string, tag_string);
                    nfc_reader.start();
                    Toast.makeText(NFCView_Activity.this, R.string.toast_get_nfc_msg_success, Toast.LENGTH_SHORT).show();
                    NFCView_Activity.this.finish();
                    break;
            }
        }
    };

    private class changeThread extends Thread {
        public void run() {
            Message msg;

            for (int i = 0; i < all_count; i++) {
                msg = new Message();
                msg.what = STEP_CAHNGE;
                myHandler.sendMessage(msg);
                SystemClock.sleep(800);
            }

            msg = new Message();
            msg.what = END_CAHNGE;
            myHandler.sendMessage(msg);
        }
    }
}
