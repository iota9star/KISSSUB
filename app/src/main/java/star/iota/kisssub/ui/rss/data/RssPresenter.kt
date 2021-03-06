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

package star.iota.kisssub.ui.rss.data

import com.lzy.okgo.model.Response
import org.jsoup.Jsoup
import star.iota.kisssub.base.StringContract
import star.iota.kisssub.base.StringPresenter
import star.iota.kisssub.room.Record

class RssPresenter(view: StringContract.View<ArrayList<Record>>) : StringPresenter<ArrayList<Record>>(view) {
    override fun deal(resp: Response<String>): ArrayList<Record> {
        val items = Jsoup.parse(resp.body())?.select("item")
        val list = ArrayList<Record>()
        items?.forEach {
            val bean = Record()
            bean.date = it?.select("pubdate")?.text()?.replace("+0800", "")
            bean.category = it?.select("category")?.text()
            val title = it?.select("title")?.text()?.replace("<![CDATA[", "")?.replace("]]>", "")
            bean.title = ("/" + title?.replace(Regex("]\\s*\\[|\\[|]|】\\s*【|】|【"), "/") + "/").replace(Regex("/\\s*/+"), "/")
            val torrent = it?.select("enclosure")?.attr("url")
            val hash = torrent?.substring(torrent.lastIndexOf("hash=") + 4, torrent.length)
            bean.magnet = "magnet:?xt=urn:btih:$hash&tr=http://open.acgtracker.com:1096/announce"
            bean.url = it?.select("guid")?.text()
            bean.sub = it?.select("author")?.text()
            val desc = it?.select("description")?.html()?.replace("<![CDATA[", "")?.replace("]]>", "")?.replace("&gt;", ">")?.replace("&lt;", "<")
            bean.desc = desc
            bean.cover = Jsoup.parseBodyFragment(desc)?.select("img")?.first()?.attr("src")
            if (bean.cover.isNullOrBlank()) {
                bean.type = Record.NO_IMAGE
            } else {
                bean.type = Record.WITH_IMAGE
            }
            list.add(bean)
        }
        return list
    }
}
