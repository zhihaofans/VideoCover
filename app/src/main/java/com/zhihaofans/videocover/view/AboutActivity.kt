package com.zhihaofans.videocover.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.facebook.drawee.backends.pipeline.Fresco
import com.zhihaofans.videocover.R
import com.zhihaofans.videocover.util.SysUtil
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.content_about.*
import org.jetbrains.anko.*

class AboutActivity : AppCompatActivity(), AnkoLogger {
    private val sys = SysUtil()
    private val set = sys.setting()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar_about)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        sys.setContext(this)
        initList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        //重写ToolBar返回按钮的行为，防止重新打开父Activity重走生命周期方法
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun initList() {
        //set.setInit(this)
        val items: Array<String> = arrayOf(
                getString(R.string.text_cleanCaches)
        )
        lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, items)
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val itemValue: String = lv.getItemAtPosition(position) as String
            debug("选择了$itemValue($position)")
            when (position) {
                0 -> {
                    Fresco.getImagePipeline().clearCaches()
                    if (set.clearAll()) {
                        toast(R.string.text_ok)
                    } else {
                        toast(R.string.text_no)
                    }
                }
                else -> toast(R.string.error)
            }
        }
    }
}
