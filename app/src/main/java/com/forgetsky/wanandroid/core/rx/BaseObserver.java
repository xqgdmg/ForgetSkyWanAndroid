package com.forgetsky.wanandroid.core.rx;

import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.Log;

import com.forgetsky.wanandroid.R;
import com.forgetsky.wanandroid.app.WanAndroidApp;
import com.forgetsky.wanandroid.base.view.IView;
import com.forgetsky.wanandroid.core.http.BaseResponse;
import com.forgetsky.wanandroid.core.http.exception.ServerException;
import com.forgetsky.wanandroid.utils.CommonUtils;

import io.reactivex.observers.ResourceObserver;
import retrofit2.HttpException;


public abstract class BaseObserver<T> extends ResourceObserver<BaseResponse<T>> {
    private static final String TAG = "BaseObserver";

    private IView mView;
    private String mErrorMsg;
    private boolean isShowError = true;

    protected BaseObserver(IView view) {
        this.mView = view;
    }

    protected BaseObserver(IView view, String errorMsg) {
        this.mView = view;
        this.mErrorMsg = errorMsg;
    }

    protected BaseObserver(IView view, boolean isShowError) {
        this.mView = view;
        this.isShowError = isShowError;
    }

    protected BaseObserver(IView view, String errorMsg, boolean isShowError) {
        this.mView = view;
        this.mErrorMsg = errorMsg;
        this.isShowError = isShowError;
    }

    public abstract void onSuccess(T t);

    @CallSuper
    public void onFailure(int code, String message) {
        mView.showErrorMsg(message);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        if (CommonUtils.isNetworkConnected()) {
            mView.showLoading();
        } else {
            mErrorMsg = WanAndroidApp.getContext().getString(R.string.http_error);
            mView.showNoNetwork();
        }
    }

    @Override
    public final void onNext(BaseResponse<T> baseResponse) {
        if (baseResponse.getErrorCode() == BaseResponse.SUCCESS) {
            onSuccess(baseResponse.getData());
        } else {
            Log.d(TAG, "onFailure");
            onFailure(baseResponse.getErrorCode(), baseResponse.getErrorMsg());
        }
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "onComplete");
        mView.hideLoading();
        mView.showContent();
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG, "onError");
        mView.hideLoading();
        if (mView == null) {
            return;
        }
        if (mErrorMsg != null && !TextUtils.isEmpty(mErrorMsg)) {
            mView.showErrorMsg(mErrorMsg);
        } else if (e instanceof ServerException) {
            mView.showErrorMsg(e.toString());
        } else if (e instanceof HttpException) {
            mView.showErrorMsg(WanAndroidApp.getContext().getString(R.string.http_error));
        } else {
            mView.showErrorMsg(WanAndroidApp.getContext().getString(R.string.unKnown_error));
//            LogHelper.d(e.toString());
        }
        if (isShowError) {
            mView.showError();
        }
    }

}
