package top.yuzhida.zccommandviewer

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.json.JSONObject
import top.yuzhida.zccommandviewer.Common.Companion.currDes

class MainActivity : AppCompatActivity()
{

    private lateinit var dialog: AlertDialog
    private lateinit var btnMin: Button
    private lateinit var edtDes: EditText
    private lateinit var text: TextView

    /*用于解析和显示json*/
    fun parseJson(jsonString: String)
    {
        text = findViewById(R.id.textView)
        text.setText("解析内容：待输入json")
        try {
            val jsonObject = JSONObject(jsonString)

            //读取特定的字段
            val name = jsonObject.getString("author")
            val jsonver = jsonObject.getString("jsonversion")
            val mcver = jsonObject.getString("mcversion")
            //显示解析结果
            text.setText("解析出指令组详情：\n作者: $name\njson版本：$jsonver\nmc版本：$mcver")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*创建事件*/
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnMin = findViewById(R.id.btnMin)
        edtDes = findViewById(R.id.edt_des)
        /*检测悬浮窗是否开启*/
        if(isServiceRunning())
        {
            stopService(Intent(this@MainActivity,FloatingWindowApp::class.java))
        }
        /*设置内容*/
        edtDes.setText(currDes)
        edtDes.setSelection(edtDes.text.toString().length)
        /*每次写东西需要保存*/
        edtDes.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                currDes = edtDes.text.toString() //保存
                parseJson(edtDes.text.toString()) //json解析
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        /*开启悬浮窗*/
        btnMin.setOnClickListener {
            if(checkOverlayPermission())//检查权限
            {
                startService(Intent(this@MainActivity,FloatingWindowApp::class.java))
                finish()
            }
            else //申请权限
            {
                requestFloatingWindowPermission()
            }
        }

        /*
        * isServiceRunning()
        * requestPermission()
        * checkAndroidVersion()
        */
    }
    /*检查是否运行中*/
    private fun isServiceRunning(): Boolean
    {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for(service in manager.getRunningServices(Int.MAX_VALUE))
        {
            if(FloatingWindowApp::class.java.name == service.service.className)
                return true
        }
        return false
    }

    /*申请权限*/
    private fun requestFloatingWindowPermission()
    {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("需要允许悬浮窗权限")
        builder.setMessage("你需要在设置中打开这个权限，权限用于在游戏中查看命令组")
        builder.setPositiveButton("Open Settings",DialogInterface.OnClickListener(){ dialog, which ->  
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, RESULT_OK)
        })
        dialog = builder.create()
        dialog.show()
    }

    /*检查权限*/
    private fun checkOverlayPermission(): Boolean
    {
        return if(Build.VERSION.SDK_INT >Build.VERSION_CODES.M)
        {
            Settings.canDrawOverlays(this)
        }
        else return true
    }
}