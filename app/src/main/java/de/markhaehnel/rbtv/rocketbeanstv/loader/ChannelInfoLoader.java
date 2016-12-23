package de.markhaehnel.rbtv.rocketbeanstv.loader;

import android.net.Network;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import de.markhaehnel.rbtv.rocketbeanstv.events.ChannelInfoUpdateEvent;
import de.markhaehnel.rbtv.rocketbeanstv.objects.RBTV;
import de.markhaehnel.rbtv.rocketbeanstv.objects.schedule.ScheduleItem;
import de.markhaehnel.rbtv.rocketbeanstv.utils.Authentication;
import de.markhaehnel.rbtv.rocketbeanstv.utils.Enums.EventStatus;
import de.markhaehnel.rbtv.rocketbeanstv.utils.NetworkHelper;

public class ChannelInfoLoader extends Thread {

    private String key, secret;

    public ChannelInfoLoader(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public void run() {
        //noinspection LoopStatementThatDoesntLoop
        while (true) {
            try {

                String url = "https://api.rocketmgmt.de/schedule/current";
                String urlViewer = "https://node.markhaehnel.de/rbtv/";

                String response = NetworkHelper.getContentFromUrl(url, Authentication.getAuthenticationHeaders(key, secret));
                String responseViewer = NetworkHelper.getContentFromUrl(urlViewer);

                Gson gson = new Gson();
                ScheduleItem scheduleItem = gson.fromJson(response, ScheduleItem.class);
                RBTV rbtv = gson.fromJson(responseViewer, RBTV.class);
                EventBus.getDefault().post(new ChannelInfoUpdateEvent(scheduleItem, rbtv, EventStatus.OK));
                sleep(30000);
            } catch (Exception e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
                EventBus.getDefault().post(new ChannelInfoUpdateEvent(EventStatus.FAILED));
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return;
            }
        }
    }
}
