package com.example.banmusicplayer

import android.app.AlertDialog
import android.app.Instrumentation
import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.banmusicplayer.base.StaticInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainFragmentActivity : MyBaseActivity() {
    private var menu: Menu? = null
    private var fm: FragmentManager? = null
    private var ft: FragmentTransaction? = null
    private var proDialog: ProgressDialog? = null
    private var rightFragment: MyMusicListFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.fragementactivity_main)
        alertDialog = AlertDialog.Builder(this)

        inti()
        proDialog = ProgressDialog(this)
        rightFragment = MyMusicListFragment()
        fm = supportFragmentManager
        defaultCheckedFragment()
        //菜单初始化
        onCreateOptionsMenu(menu)
    }

    private fun defaultCheckedFragment() {
        ft = fm!!.beginTransaction()
        ft!!.replace(R.id.fragment_layout, rightFragment!!).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu
        if (menu != null) {
            menu.clear()
        } else {
            return true
        }

        menuInflater.inflate(R.menu.main2, menu)
        if (menu.javaClass.simpleName == "MenuBuilder") {
            //反射
            try {
                val m = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible", Boolean::class.javaPrimitiveType
                )
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("关于").setView(StaticInfo.getAboutContent(this))
            dialog.setNegativeButton(R.string.Sys_confirm) { dialog, which -> dialog.cancel() }
                .show()
            return super.onOptionsItemSelected(item)
        } else if (item.itemId == R.id.action_add_list) {
            rightFragment!!.addList()
            return super.onOptionsItemSelected(item)
        }
        proDialog?.apply {
            setMessage("正在载入音乐...")
            setCancelable(false)
            isIndeterminate = true
            show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (proDialog!!.isShowing) {
            proDialog!!.dismiss()
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (StaticInfo.db != null) {
            StaticInfo.db.close()
        }
    }

    override fun initView() {
    }

    override fun initButton() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            GlobalScope.launch {
                //等于按下菜单键
                Instrumentation().sendKeyDownUpSync(82)
            }
        }
    }

    override fun initListView() {}
}