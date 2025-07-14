package kr.co.skeleton;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Map;

import kr.co.skeleton.common.Constant;
import kr.co.skeleton.common.PrefManager;
import kr.co.skeleton.ui.signin.SignInActivity;

//firebase 라이브러리 설치 후 사용
/*
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "";
        String message = "";

        Map<String, String> param = remoteMessage.getData();
        if (!param.isEmpty()) {
            title = param.get("title");
            message = param.get("message");
        }

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            title = notification.getTitle();
            message = notification.getBody();
        }
        showNotification(title, message);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        PrefManager.setFbToken(getApplicationContext(), s);
    }

    private void showNotification(String title, String msg) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(msg)) {
            return;
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constant.CHANNEL_ID, Constant.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("ssafy channel");
            notificationManagerCompat.createNotificationChannel(channel);
        }

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, SignInActivity.class));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder bd = new NotificationCompat.Builder(this, Constant.CHANNEL_ID);
        bd.setContentIntent(pendingIntent);
        bd.setContentTitle(title);
        bd.setContentText(msg);
        bd.setDefaults(Notification.DEFAULT_ALL);
        bd.setAutoCancel(true);
        bd.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        bd.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        bd.setSmallIcon(R.drawable.ico_login_logo);
        notificationManagerCompat.notify(0, bd.build());
    }
}
*/
