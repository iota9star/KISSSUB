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

import android.annotation.SuppressLint
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_rss_tag_manage.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import star.iota.kisssub.R
import star.iota.kisssub.base.BaseFragment
import star.iota.kisssub.eventbus.ChangeAdapterEvent
import star.iota.kisssub.room.AppDatabaseHelper
import star.iota.kisssub.room.RssTag
import star.iota.kisssub.widget.M

class RssTagManageFragment : BaseFragment(), RssTagManageContract.View {

    override fun success(rssTag: RssTag) {
        M.create(activity().applicationContext, "添加成功")
        adapter.add(rssTag)
    }

    override fun success(tags: ArrayList<RssTag>) {
        end()
        adapter.addAll(tags)
    }

    override fun error(e: String?) {
        end()
        M.create(activity().applicationContext, e)
    }

    override fun noData() {
        end()
        M.create(activity().applicationContext, "您还没有添加订阅关键字")
    }

    companion object {
        fun newInstance() = RssTagManageFragment()
    }

    private fun end() {
        isLoading = false
        refreshLayout?.finishRefresh()
    }

    override fun getBackgroundView(): ImageView = imageViewContentBackground
    override fun getMaskView(): View = viewMask
    override fun getContainerViewId(): Int = R.layout.fragment_rss_tag_manage

    override fun doSome() {
        setToolbarTitle("订阅管理")
        initRecyclerView()
        initRefreshLayout()
        initActionView()
    }

    private fun initActionView() {
        imageViewAdd.setOnClickListener {
            showAddDialog()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdapterDataChange(event: ChangeAdapterEvent) {
        when (event.type) {
            ChangeAdapterEvent.DELETE -> adapter.remove(event.pos)
            ChangeAdapterEvent.MODIFY -> {
                if (!isLoading) refreshLayout?.autoRefresh()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showAddDialog() {
        val view = LayoutInflater.from(activity()).inflate(R.layout.dialog_add_rss_tag, null)
        val editText = view.findViewById<TextInputEditText>(R.id.textInputEditTextRssTag)
        val dialog = AlertDialog.Builder(activity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("添加订阅")
                .setView(view)
                .setNegativeButton("添加", null)
                .setPositiveButton("取消", { dialog, _ ->
                    dialog.dismiss()
                })
                .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            val tag = editText.text.toString()
            if (tag.trim().isBlank()) {
                editText.error = "不能为空"
            } else {
                val rssTag = RssTag()
                rssTag.tag = tag
                presenter.add(AppDatabaseHelper.getInstance(activity().applicationContext), rssTag)
                dialog.dismiss()
            }
        }
    }


    private val presenter: RssTagManagePresenter by lazy {
        RssTagManagePresenter(this)
    }
    private var isLoading = false
    private fun initRefreshLayout() {
        refreshLayout?.autoRefresh()
        refreshLayout?.isEnableLoadMore = false
        refreshLayout?.setOnRefreshListener {
            if (!checkIsLoading()) {
                isLoading = true
                adapter.clear()
                presenter.get(AppDatabaseHelper.getInstance(activity().applicationContext))
            }
        }
    }

    private fun checkIsLoading(): Boolean = if (isLoading) {
        M.create(activity().applicationContext, "数据正在加载中，请等待...")
        true
    } else {
        false
    }

    private val adapter: RssTagAdapter by lazy { RssTagAdapter() }
    private fun initRecyclerView() {
        recyclerView?.layoutManager = LinearLayoutManager(activity(), LinearLayoutManager.VERTICAL, false)
        recyclerView?.itemAnimator = LandingAnimator()
        recyclerView?.adapter = adapter
    }

    override fun onDestroy() {
        presenter.unsubscribe()
        super.onDestroy()
    }
}
