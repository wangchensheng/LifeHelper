package com.ns.yc.lifehelper.ui.other.gank.presenter;

import com.blankj.utilcode.util.ToastUtils;
import com.ns.yc.lifehelper.base.BaseApplication;
import com.ns.yc.lifehelper.ui.other.gank.bean.GanKFavorite;
import com.ns.yc.lifehelper.ui.other.gank.contract.GanKWebContract;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.subscriptions.CompositeSubscription;

/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/9/14
 * 描    述：干货集中营详情页面
 * 修订历史：
 * ================================================
 */
public class GanKWebPresenter implements GanKWebContract.Presenter {

    private GanKWebContract.View mView;
    private CompositeSubscription mSubscriptions;
    private String mUrl;
    private boolean mIsFavorite;
    private GanKFavorite favoriteData;
    private Realm realm;


    public GanKWebPresenter(GanKWebContract.View view) {
        this.mView = view;
        mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void subscribe() {
        initView();
    }

    @Override
    public void unSubscribe() {
        mSubscriptions.clear();
    }

    private void initView() {
        // 设置Fab的背景色
        mView.setFabColor();
        //获取收藏序列化内存
        favoriteData = mView.getFavoriteData();
        //查看数据库是否是收藏内容
        findHasFavorite();
        //加载内容
        loadData();
        //加载数据库
        initRealm();
    }

    private void initRealm() {
        realm = BaseApplication.getInstance().getRealmHelper();
    }


    private void loadData() {
        mUrl = mView.getLoadUrl();
        mView.loadURL(mUrl);
    }

    /**
     * 处理收藏逻辑
     */
    @Override
    public void meFavorite() {
        if (mIsFavorite) {          // 已经收藏
            unFavorite();
        } else {                    // 未收藏
            favorite();
        }
    }

    /**
     * 收藏
     */
    private void favorite() {
        favoriteData.setCreatetime(System.currentTimeMillis());
        mIsFavorite = favoriteData.isManaged();
        mView.setFavoriteState(mIsFavorite);
        if (!mIsFavorite) {
            ToastUtils.showShort("收藏失败,请重试");
        }else {
            ToastUtils.showShort("收藏成功");
        }
    }

    /**
     * 取消收藏
     */
    private void unFavorite() {
        if(realm!=null){
            mIsFavorite = realm.where(GanKFavorite.class)
                    .equalTo("gankID", favoriteData.getGankID())
                    .findAll()
                    .deleteAllFromRealm();
        }
        // 不调用这句保存 在保存会失败，并且返回的是true
        favoriteData.deleteFromRealm();
        mView.setFavoriteState(mIsFavorite);
        if (mIsFavorite) {
            ToastUtils.showShort("取消收藏失败,请重试");
        }
    }

    /**
     * 是否有收藏
     */
    private void findHasFavorite() {
        if (favoriteData == null) {
            // 隐藏收藏 fab
            mView.hideFavoriteFab();
            return;
        }
        if(realm!=null && realm.where(GanKFavorite.class)!=null){
            RealmResults<GanKFavorite> favorites = realm.where(GanKFavorite.class)
                    .equalTo("gankID", favoriteData.getGankID())
                    .findAll();
            mIsFavorite = favorites.size() > 0;
            mView.setFavoriteState(mIsFavorite);
        }
    }


}
