package com.pallaw.flickrgallery.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.pallaw.flickrgallery.data.model.Photo
import com.pallaw.flickrgallery.data.remote.ApiService
import com.pallaw.flickrgallery.data.remote.FIRST_PAGE
import com.pallaw.flickrphotos.util.NetworkState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Pallaw Pathak on 21/04/20. - https://www.linkedin.com/in/pallaw-pathak-a6a324a1/
 */
const val DEFAULT_SEARCH_TAG = "android"

class PhotoDataSource(
    private val tag: String,
    private val apiService: ApiService,
    private val compositeDisposable: CompositeDisposable
) : PageKeyedDataSource<Int, Photo>() {

    private var page = FIRST_PAGE
    private var TAG = PhotoDataSource::class.java.simpleName

    val networkState: MutableLiveData<NetworkState> = MutableLiveData()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Photo>
    ) {

        networkState.postValue(NetworkState.LOADING)

        /*
        if search tag is empty then use recentPhotoApi otherwise use searchApi for page 1
         */
        if (tag.isEmpty()) {
            compositeDisposable.add(searchApiInitial(DEFAULT_SEARCH_TAG, page, callback))
        } else {
            compositeDisposable.add(searchApiInitial(tag, page, callback))
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
        networkState.postValue(NetworkState.LOADING)

        /*
        if search tag is empty then then use default search tag
         */
        if (tag.isEmpty()) {
            compositeDisposable.add(searchApiLoadMore(DEFAULT_SEARCH_TAG, params.key, callback))
        } else {
            compositeDisposable.add(searchApiLoadMore(tag, params.key, callback))
        }
    }

    private fun searchApiLoadMore(
        searchTag: String,
        page: Int,
        loadCallback: LoadCallback<Int, Photo>
    ): Disposable {
        return apiService.searchTag(searchTag, page)
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    if (it.photos.pages >= page) {
                        loadCallback.onResult(it.photos.photo, page + 1)
                        networkState.postValue(NetworkState.LOADED)
                    } else {
                        networkState.postValue(NetworkState.ENDOFLIST)
                    }
                },
                {
                    networkState.postValue(NetworkState.ERROR)
                    Log.e(TAG, it.message)
                }
            )
    }

//    private fun recentPhotoApiLoadMore(
//        page: Int,
//        loadMoreCallback: LoadCallback<Int, Photo>
//    ): Disposable {
//        return apiService.getRecentPhotos(page)
//            .subscribeOn(Schedulers.io())
//            .subscribe(
//                {
//                    if (it.photos.pages >= page) {
//                        loadMoreCallback.onResult(it.photos.photo, page + 1)
//                        networkState.postValue(NetworkState.LOADED)
//                    } else {
//                        networkState.postValue(NetworkState.ENDOFLIST)
//                    }
//                },
//                {
//                    networkState.postValue(NetworkState.ERROR)
//                    Log.e(TAG, it.message)
//                }
//            )
//    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {

    }

    private fun searchApiInitial(
        tag: String,
        page: Int,
        initialCallback: LoadInitialCallback<Int, Photo>
    ): Disposable {
        return apiService.searchTag(tag, page)
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    initialCallback.onResult(it.photos.photo, null, page + 1)
                    networkState.postValue(NetworkState.LOADED)
                },
                {
                    networkState.postValue(NetworkState.ERROR)
                    Log.e(TAG, it.message)
                }
            )
    }

//    private fun recentPhotoApiInitial(
//        page: Int,
//        initialCallback: LoadInitialCallback<Int, Photo>
//    ): Disposable {
//        return apiService.getRecentPhotos(page)
//            .subscribeOn(Schedulers.io())
//            .subscribe(
//                {
//                    initialCallback.onResult(it.photos.photo, null, page + 1)
//                    networkState.postValue(NetworkState.LOADED)
//                },
//                {
//                    networkState.postValue(NetworkState.ERROR)
//                    Log.e(TAG, it.message)
//                }
//            )
//    }
}