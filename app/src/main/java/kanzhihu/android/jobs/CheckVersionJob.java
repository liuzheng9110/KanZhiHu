package kanzhihu.android.jobs;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import kanzhihu.android.App;
import kanzhihu.android.AppConstant;
import kanzhihu.android.BuildConfig;
import kanzhihu.android.managers.HttpClientManager;
import kanzhihu.android.managers.NetworkManager;
import kanzhihu.android.managers.UpdateManager;
import kanzhihu.android.utils.AppLogger;
import kanzhihu.android.utils.PreferenceUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jiahui.wen on 2014/11/11.
 *
 * 检查app的最新版本
 */
public class CheckVersionJob extends Job {

    private static final String TAG = CheckVersionJob.class.getSimpleName();

    public CheckVersionJob() {
        super(new Params(Priority.LOW));
    }

    @Override public void onAdded() {

    }

    @Override public void onRun() throws Throwable {
        if (!NetworkManager.isConnected()) {
            return;
        }

        Request request = new Request.Builder().url(AppConstant.APP_INFO_URL).build();
        Response response = HttpClientManager.request(request);
        try {
            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                compareVersion(jsonObject);
            }
        } finally {
            response.body().close();
        }
    }

    private void compareVersion(JSONObject jsonObject) {
        //todo addd update messages
        int lastestVersion = Integer.parseInt(jsonObject.optString("lastest_version", "0"));
        if (lastestVersion > BuildConfig.VERSION_CODE) {
            //服务器上有新版本
            try {
                int ignoreVersion = PreferenceUtils.getInt(AppConstant.KEY_IGNORE_VERSION, -1);
                if (lastestVersion == ignoreVersion) {
                    //忽略此版本
                    return;
                }

                //保留当前最新版本号
                PreferenceUtils.setInt(AppConstant.KEY_NEW_VERSION, lastestVersion);

                UpdateManager.setUpdateUrl(jsonObject.getString("url"));
                UpdateManager.registerUpdateBroadcast();
                LocalBroadcastManager.getInstance(App.getAppContext())
                    .sendBroadcast(new Intent(AppConstant.ACTION_NEW_VERSION_APP));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override protected void onCancel() {

    }

    @Override protected boolean shouldReRunOnThrowable(Throwable throwable) {
        AppLogger.e(TAG, throwable);
        return false;
    }
}
