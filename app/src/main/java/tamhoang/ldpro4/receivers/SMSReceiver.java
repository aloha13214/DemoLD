package tamhoang.ldpro4.receivers;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import tamhoang.ldpro4.Congthuc.Congthuc;
import tamhoang.ldpro4.MainActivity;
import tamhoang.ldpro4.R;
import tamhoang.ldpro4.data.Database;
import tamhoang.ldpro4.services.SaveSmsService;

public class SMSReceiver extends BroadcastReceiver {
    String Ten_KH;
    String body = "";
    JSONObject caidat_gia;
    JSONObject caidat_tg;
    Database db;
    JSONObject json;
    Context mContext;
    String mGionhan;
    String mNgayNhan;
    String mSDT;
    SmsMessage[] messages = null;
    int soTN;

    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0479, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x047a, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x035d, code lost:
        r28.db.QueryData("Update tbl_tinnhanS set phat_hien_loi = 'ko' WHERE id = " + r3.getInt(0));
        r28.db.QueryData("Delete From tbl_soctS WHERE ngay_nhan = '" + r28.mNgayNhan + "' AND so_dienthoai = '" + r28.mSDT + "' AND so_tin_nhan = " + r28.soTN + " AND type_kh = 1");
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:71:0x023d, B:94:0x03cf] */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0479 A[ExcHandler: JSONException (r0v4 'e' org.json.JSONException A[CUSTOM_DECLARE]), Splitter:B:71:0x023d] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x012a A[Catch:{ Exception -> 0x0130 }] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x023d A[SYNTHETIC, Splitter:B:71:0x023d] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x03cc A[ExcHandler: SQLException (e android.database.SQLException), Splitter:B:71:0x023d] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x03cf A[SYNTHETIC, Splitter:B:94:0x03cf] */
    public void onReceive(Context context, Intent intent) {
        String trim;
        boolean Ktra;
        JSONException e;
        this.db = new Database(context);
        this.mContext = context;
        boolean Ktra2 = true;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            this.messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                this.messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                SmsMessage currentSMS = getIncomingMessage(pdus[i], bundle);
                issueNotification(context, currentSMS.getDisplayOriginatingAddress(), currentSMS.getDisplayMessageBody());
                saveSmsInInbox(context, currentSMS);
            }
            SmsMessage sms = this.messages[0];
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
            dmyFormat.setTimeZone(TimeZone.getDefault());
            hourFormat.setTimeZone(TimeZone.getDefault());
            this.mNgayNhan = dmyFormat.format(calendar.getTime());
            this.mGionhan = hourFormat.format(calendar.getTime());
            this.mSDT = "";
            try {
                if (this.messages.length != 1) {
                    try {
                        if (!sms.isReplace()) {
                            StringBuilder bodyText = new StringBuilder();
                            for (int i2 = 0; i2 < this.messages.length; i2++) {
                                bodyText.append(this.messages[i2].getMessageBody());
                            }
                            this.body = bodyText.toString().replace("'", "");
                            trim = sms.getDisplayOriginatingAddress().replace(" ", "").trim();
                            this.mSDT = trim;
                            if (trim.startsWith("0")) {
                                this.mSDT = "+84" + this.mSDT.substring(1);
                            }
                            if (MainActivity.DSkhachhang.size() == 0) {
                                this.db.LayDanhsachKH();
                            }
                            if (!(MainActivity.DSkhachhang.contains(this.mSDT)
                                    || this.body.indexOf("Ok") == 0
                                    || this.body.indexOf("B???") == 0
                                    || this.body.indexOf("Thi???u") == 0)
                                    || this.body.contains("Tra lai")) {
                                MainActivity.sms = true;
                                try {
                                    if (MainActivity.jSon_Setting.getInt("tin_trung") > 0) {
                                        Cursor Ktratin = this.db.GetData("Select id From tbl_tinnhanS WHERE so_dienthoai = '" + this.mSDT + "' AND ngay_nhan = '" + this.mNgayNhan + "' AND nd_goc = '" + this.body + "'");
                                        Ktratin.moveToFirst();
                                        if (Ktratin.getCount() > 0) {
                                            Ktra2 = false;
                                        }
                                        if (!Ktratin.isClosed()) {
                                            Ktratin.close();
                                        }
                                    }
                                } catch (JSONException e2) {
                                    Log.d(SMSReceiver.class.getName(), e2.getMessage());
                                }
                                try {
                                    Cursor getTenKH = this.db.GetData("Select * FROM tbl_kh_new WHERE sdt ='" + this.mSDT + "'");
                                    getTenKH.moveToFirst();
                                    if (Ktra2) {
                                        try {
                                            JSONObject jSONObject = new JSONObject(getTenKH.getString(5));
                                            this.json = jSONObject;
                                            this.caidat_gia = jSONObject.getJSONObject("caidat_gia");
                                            this.caidat_tg = this.json.getJSONObject("caidat_tg");
                                        } catch (JSONException e3) {
                                            Log.d(SMSReceiver.class.getName(), e3.getMessage());
                                        }
                                        try {
                                            Ktra = Ktra2;
                                            if (Congthuc.CheckTime(this.caidat_tg.getString("tg_debc"))) {
                                                try {
                                                    Cursor getSoTN = this.db.GetData("Select max(so_tin_nhan) from tbl_tinnhanS WHERE ngay_nhan = '" + this.mNgayNhan + "' AND so_dienthoai = '" + this.mSDT + "' AND type_kh = 1");
                                                    getSoTN.moveToFirst();
                                                    this.Ten_KH = getTenKH.getString(0);
                                                    this.soTN = getSoTN.getInt(0) + 1;
                                                    this.db.QueryData(this.body.indexOf("Tra lai") == -1 ? "Insert Into tbl_tinnhanS values (null, '" + this.mNgayNhan + "', '" + this.mGionhan + "',1, '" + this.Ten_KH + "', '" + getTenKH.getString(1) + "','sms', " + this.soTN + ", '" + this.body + "',null,'" + this.body + "', 'ko',0,1,1, null)" : "Insert Into tbl_tinnhanS values (null, '" + this.mNgayNhan + "', '" + this.mGionhan + "',1, '" + this.Ten_KH + "', '" + getTenKH.getString(1) + "','sms', " + this.soTN + ", '" + this.body + "',null,'" + this.body + "', 'ko',0,0,0, null)");
                                                    if (Congthuc.CheckDate(MainActivity.myDate)) {
                                                        Cursor c = this.db.GetData("Select * from tbl_tinnhanS WHERE ngay_nhan = '" + this.mNgayNhan + "' AND so_dienthoai = '" + this.mSDT + "' AND so_tin_nhan = " + this.soTN + " AND type_kh = 1");
                                                        c.moveToFirst();
                                                        this.db.Update_TinNhanGoc(c.getInt(0), 1);
                                                        if (!Congthuc.CheckTime("18:30") && this.body.indexOf("Tra lai") == -1) {
                                                            this.db.Gui_Tin_Nhan(c.getInt(0));
                                                        }
                                                        c.close();
                                                    }
                                                    if (getSoTN != null && !getSoTN.isClosed()) {
                                                        getSoTN.close();
                                                    }
                                                } catch (SQLException e5) {
                                                    Log.d(SMSReceiver.class.getName(), e5.getMessage());
                                                } catch (Throwable e6) {
                                                    Log.d(SMSReceiver.class.getName(), e6.getMessage());
                                                }
                                            } else {
                                                Cursor getSoTN2 = this.db.GetData("Select max(so_tin_nhan) from tbl_tinnhanS WHERE ngay_nhan = '" + this.mNgayNhan + "' AND so_dienthoai = '" + this.mSDT + "' AND type_kh = 1");
                                                getSoTN2.moveToFirst();
                                                this.Ten_KH = getTenKH.getString(0);
                                                this.soTN = getSoTN2.getInt(0) + 1;
                                                this.db.QueryData("Insert Into tbl_tinnhanS values (null, '" + this.mNgayNhan + "', '" + this.mGionhan + "',1, '" + this.Ten_KH + "', '" + getTenKH.getString(1) + "','sms', " + this.soTN + ", '" + this.body + "',null,'" + this.body + "', 'H???t gi??? nh???n s???!',0,1,1, null)");
                                                if (getSoTN2 != null && !getSoTN2.isClosed()) {
                                                    getSoTN2.close();
                                                }
                                                if (!Congthuc.CheckTime("18:30") && MainActivity.jSon_Setting.getInt("tin_qua_gio") == 1) {
                                                    this.db.SendSMS(getTenKH.getString(1), "H???t gi??? nh???n!");
                                                }
                                            }
                                        } catch (JSONException e7) {
                                            Ktra = Ktra2;
                                            JSONException e8 = e7;
                                            Log.d(SMSReceiver.class.getName(), e7.getMessage());
                                            try {
                                                e8.printStackTrace();
                                                getTenKH.close();
                                                return;
                                            } catch (Exception e9) {
                                                Log.d(SMSReceiver.class.getName(), e9.getMessage());
                                                return;
                                            }
                                        }
                                    } else {
                                        Ktra = Ktra2;
                                    }
                                    if (!getTenKH.isClosed()) {
                                        getTenKH.close();
                                    }
                                    return;
                                } catch (Exception e10) {
                                    Log.d(SMSReceiver.class.getName(), e10.getMessage());
                                    return;
                                }
                            }
                        }
                    } catch (Exception e11) {
                        Log.d(SMSReceiver.class.getName(), e11.getMessage());
                        return;
                    }
                }
                this.body = sms.getDisplayMessageBody().replace("'", "");
                trim = sms.getDisplayOriginatingAddress().replace(" ", "").trim();
                this.mSDT = trim;
//                if (trim.startsWith("0")) {
//                }
//                try {
//                    if (MainActivity.DSkhachhang.size() == 0) {
//                    }
//                } catch (Exception e12) {
//                }
//                if (MainActivity.DSkhachhang.indexOf(this.mSDT) > -1) {
//                }
            } catch (Exception e13) {
                Log.d(SMSReceiver.class.getName(), e13.getMessage());
            }
        }
    }

    private void saveSmsInInbox(Context context, SmsMessage sms) {
        Intent serviceIntent = new Intent(context, SaveSmsService.class);
        serviceIntent.putExtra("sender_no", sms.getDisplayOriginatingAddress());
        serviceIntent.putExtra("message", sms.getDisplayMessageBody());
        serviceIntent.putExtra("date", sms.getTimestampMillis());
        context.startService(serviceIntent);
    }

    @SuppressLint("WrongConstant")
    private void issueNotification(Context context, String senderNo, String message) {
        ((NotificationManager) context.getSystemService("notification")).notify(101, new NotificationCompat.Builder(context).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher)).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(senderNo).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setAutoCancel(true).setContentText(message).build());
    }

    /* JADX INFO: Multiple debug info for r0v7 android.telephony.SmsMessage: [D('format' java.lang.String), D('currentSMS' android.telephony.SmsMessage)] */
    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        if (Build.VERSION.SDK_INT < 23) {
            return SmsMessage.createFromPdu((byte[]) aObject);
        }
        return SmsMessage.createFromPdu((byte[]) aObject, bundle.getString("format"));
    }
}