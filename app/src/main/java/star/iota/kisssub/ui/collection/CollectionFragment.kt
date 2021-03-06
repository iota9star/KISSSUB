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

package star.iota.kisssub.ui.collection

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_default.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import star.iota.kisssub.R
import star.iota.kisssub.base.BaseFragment
import star.iota.kisssub.eventbus.ChangeAdapterEvent
import star.iota.kisssub.room.AppDatabaseHelper
import star.iota.kisssub.room.Record
import star.iota.kisssub.widget.M

class CollectionFragment : BaseFragment(), CollectionContract.View {
    override fun success(items: ArrayList<Record>) {
        end()
        adapter.addAll(items)
    }

    override fun error(e: String?) {
        end()
        M.create(activity().applicationContext, e)
    }

    override fun noData() {
        end()
        M.create(activity().applicationContext, "您还没有收藏哦")
    }

    companion object {
        fun newInstance() = CollectionFragment()
    }

    private fun end() {
        isLoading = false
        refreshLayout?.finishRefresh()
    }


    override fun getContainerViewId(): Int = R.layout.fragment_default
    override fun getBackgroundView(): ImageView = imageViewContentBackground
    override fun getMaskView(): View = viewMask
    override fun doSome() {
        setToolbarTitle(context!!.getString(R.string.menu_favorite))
        initRecyclerView()
        initRefreshLayout()
    }

    private val presenter: CollectionPresenter by lazy { CollectionPresenter(this) }

    private var isLoading = false
    private fun initRefreshLayout() {
        refreshLayout?.autoRefresh()
        refreshLayout?.isEnableLoadMore = false
        refreshLayout?.setOnRefreshListener {
            if (!checkIsLoading()) {
                isLoading = true
                adapter.clear()
                presenter.get(AppDatabaseHelper.getInstance(context!!))
            }
        }
    }

    private fun checkIsLoading(): Boolean {
        if (isLoading) {
            M.create(activity().applicationContext, "数据正在加载中，请等待...")
            return true
        }
        return false
    }

    private val adapter: CollectionAdapter by lazy { CollectionAdapter() }
    private fun initRecyclerView() {
        recyclerView?.layoutManager = LinearLayoutManager(activity(), LinearLayoutManager.VERTICAL, false)
        recyclerView?.itemAnimator = LandingAnimator()
        recyclerView?.adapter = adapter
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdapterDataChange(event: ChangeAdapterEvent) {
        when (event.type) {
            ChangeAdapterEvent.DELETE -> adapter.remove(event.pos)
        }
    }

    override fun onDestroy() {
        presenter.unsubscribe()
        super.onDestroy()
    }

}
