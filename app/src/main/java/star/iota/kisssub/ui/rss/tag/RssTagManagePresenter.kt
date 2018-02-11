/*
 *
 *  *    Copyright 2018. iota9star
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package star.iota.kisssub.ui.rss.tag

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import star.iota.kisssub.room.AppDatabaseHelper
import star.iota.kisssub.room.RssTag


class RssTagManagePresenter(private val view: RssTagManageContract.View) : RssTagManageContract.Presenter() {
    override fun get(helper: AppDatabaseHelper) {
        compositeDisposable.add(
                Single.just(helper)
                        .map { it.allRssTag() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (it == null || it.isEmpty()) {
                                view.noData()
                            } else {
                                view.success(it as ArrayList<RssTag>)
                            }
                        }, {
                            view.error(it?.message)
                        })
        )
    }

    override fun add(helper: AppDatabaseHelper, rssTag: RssTag) {
        compositeDisposable.add(
                Single.just(helper)
                        .map { it.addRssTag(rssTag) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            view.success(rssTag)
                        }, {
                            view.error(it?.message)
                        })
        )
    }

    companion object {
        private val compositeDisposable = CompositeDisposable()
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }
}
