package kanzhihu.android.activities.presenters.impl;

import android.app.LoaderManager;
import android.database.Cursor;
import de.greenrobot.event.EventBus;
import kanzhihu.android.AppConstant;
import kanzhihu.android.activities.presenters.CategoryPresenter;
import kanzhihu.android.activities.views.CategoryView;
import kanzhihu.android.events.FetchedRssEvent;
import kanzhihu.android.events.ImageModeChangeEvent;
import kanzhihu.android.events.ListitemClickEvent;
import kanzhihu.android.jobs.FetchRssJob;
import kanzhihu.android.managers.BackThreadManager;
import kanzhihu.android.utils.AssertUtils;
import kanzhihu.android.utils.PreferenceUtils;

/**
 * Created by Jiahui.wen on 2014/11/13.
 */
public class CategoryPresenterImpl implements CategoryPresenter {

    private CategoryView mView;

    public CategoryPresenterImpl(CategoryView mView) {
        this.mView = AssertUtils.requireNonNull(mView, CategoryView.class.getSimpleName() + " must not be null");

        init();
    }

    @Override public void init() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override public void onEventMainThread(ImageModeChangeEvent event) {

    }

    @Override public void onEventMainThread(FetchedRssEvent event) {
        mView.hideFetchRssUI();
    }

    @Override public void onEventMainThread(ListitemClickEvent event) {
        if (mView.getVisiable()) {
            mView.showArticles(event.position);
        }
    }

    @Override public void fetchRss() {
        mView.showFetchRssUI();
        BackThreadManager.getJobManager().addJobInBackground(new FetchRssJob());
    }

    @Override public void loadDataFromDB(LoaderManager.LoaderCallbacks callbacks) {
        mView.getLoaderManager().initLoader(AppConstant.ID_CATEGORY_LOADER, null, callbacks);
    }

    @Override public void loadDataFromDBComplete(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            fetchRss();
        } else {
            boolean isAutoFetchRss = PreferenceUtils.isAutoFetchRss();
            if (isAutoFetchRss) {
                fetchRss();
            }
        }
    }

    @Override public void unloadDataFromDb() {
        mView.getLoaderManager().destroyLoader(AppConstant.ID_CATEGORY_LOADER);
    }

    @Override public void onDestory() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        unloadDataFromDb();
    }
}
