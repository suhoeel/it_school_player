package com.itschool.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.Build;
import android.widget.RemoteViews;

import mg.utils.notify.NotificationHelper;

class Notifications implements MediaPlayerStateListener {

  /**
   * notification channel id
   */
  public static final String NOTIFICATION_CHANNEL = "nc";
  /**
   * notification id
   */
  public static final int NOTIFICATION_ID = 1;
  private static final String TAP_TO_CLOSE = "Tap to close";
  private final Service service;
  /**
   * notification for playback control
   */
  Notification notification;
  Notification.Builder builder;

  public Notifications(Service service) {
    this.service = service;
  }

  public void create() {
    if (Build.VERSION.SDK_INT >= 26) {
      /* create a notification channel */
      var name = "Playback Control";
      var description = "Notification audio controls";
      var importance = NotificationManager.IMPORTANCE_LOW;
      var notificationChannel = NotificationHelper.setupNotificationChannel(service, NOTIFICATION_CHANNEL, name, description, importance);
      notificationChannel.setSound(null, null);
      notificationChannel.setVibrationPattern(null);
    }
  }

  /**
   * setup notification properties
   *
   * @param title           title of notification (title of file)
   * @param killIntent      pending intent for closing the service
   */
  void setupNotificationBuilder(String title, PendingIntent killIntent /*, PendingIntent skipIntent*/, boolean allowLoop, boolean canSkip) {

    // create builder instance
    if (Build.VERSION.SDK_INT >= 26) {
      builder = new Notification.Builder(service, NOTIFICATION_CHANNEL);
    } else {
      builder = new Notification.Builder(service);
    }

    builder.setCategory(Notification.CATEGORY_SERVICE);

    builder.setSmallIcon(R.drawable.ic_notif);
    builder.setContentTitle(title);
//    builder.addAction(new Notification.Action.Builder(Icon.createWithResource(this.service, 0), "재생", playIntent).build());
//    builder.addAction(new Notification.Action.Builder(Icon.createWithResource(this.service, 0), "정지", pauseIntent).build());
    builder.addAction(new Notification.Action.Builder(Icon.createWithResource(this.service, 0), "종료", killIntent).build());

  }

  @Override
  public void setState(boolean playing, boolean looping) {
    buildNotification();
    update();
  }

  /**
   * Generate pending intents for service control
   *
   * @param id     the id for the intent
   * @param action the control action
   * @return the pending intent generated
   */
  PendingIntent genIntent(int id, byte action) {
    /* flags for control logics on notification */
    var pendingIntentFlag = PendingIntent.FLAG_IMMUTABLE;
    pendingIntentFlag |= PendingIntent.FLAG_UPDATE_CURRENT;

    var intentFlag = Intent.FLAG_ACTIVITY_NO_HISTORY;
    intentFlag |= Intent.FLAG_ACTIVITY_NO_ANIMATION;

    return PendingIntent
      .getService(service, id, new Intent(service, Service.class)
          .addFlags(intentFlag)
          .putExtra(Launcher.TYPE, action)
        , pendingIntentFlag);
  }

  /**
   * generate new notification
   */
  void genNotification() {
    buildNotification();
  }

  /**
   * build notification from notification builder
   */
  void buildNotification() {
    notification = builder.build();
  }


  /**
   * create and start playback control notification
   */
  void getNotification(final String title, boolean canLoop, boolean canSkip) {

    /* calls for control logic by starting activity with flags */
//    var playIntent = genIntent(1, Launcher.PLAY);
//    var pauseIntent = genIntent(2, Launcher.PAUSE);
    var killIntent = genIntent(3, Launcher.KILL);
//    var skipIntent = genIntent(4, Launcher.SKIP);

    setupNotificationBuilder(title, killIntent, canLoop, canSkip);
    genNotification();

    update();
  }

  /**
   * update notification content and place on stack
   */
  private void update() {
    NotificationHelper.send(service, NOTIFICATION_ID, notification);
  }

  @Override
  public void onMediaPlayerReset() {
    /* remove notification from stack */
    NotificationHelper.unsend(service, NOTIFICATION_ID);
  }

  @Override
  public void onMediaPlayerDestroy() {

  }
}
