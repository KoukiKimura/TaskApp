package com.example.taskapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import io.realm.Realm

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?,intent: Intent?){
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // SDKバージョンが２６以上の場合、チャンネルを設定する必要がある
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Channel description"
            notificationManager.createNotificationChannel(channel)
        }

        // 通知の瀬舘を行う
        val builder = NotificationCompat.Builder(context, "default")
        builder.setSmallIcon(R.drawable.small_icon)
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.large_icon))
        builder.setWhen(System.currentTimeMillis())
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setAutoCancel(true)

        // EXTRA_TASK から Tak の　id を取得して、 id から　Task のインスタンスを取得する
        val taskId = intent!!.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        val task  = realm.where(Task::class.java).equalTo("id", taskId).findFirst()

        // タスクの除法を設定する
        builder.setTicker(task!!.title)   // 5.0 以降は表示されない
        builder.setContentTitle(task.title)
        builder.setContentText(task.contents)

        // 通知をタップしたらアプリを起動するようにする
        val startAppintent = Intent(context, MainActivity::class.java)
        startAppintent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(context, 0, startAppintent, 0)
        builder.setContentIntent(pendingIntent)

        // 通知を表示する
        notificationManager.notify(task!!.id, builder.build())
        realm.close()
    }

}