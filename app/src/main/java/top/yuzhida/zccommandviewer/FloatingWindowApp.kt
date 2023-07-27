package top.yuzhida.zccommandviewer

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import org.json.JSONObject
import top.yuzhida.zccommandviewer.Common.Companion.currDes

class FloatingWindowApp : Service(){

    private lateinit var floatView: ViewGroup
    private lateinit var floatWindowLayoutParams: WindowManager.LayoutParams
    private var LAYOUT_TYPR: Int? = null
    private lateinit var windowManager: WindowManager
    private lateinit var btnmax: Button
    private var json: String? = ""


    fun parseJson(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)

            // 读取特定的字段
            val name = jsonObject.getString("author")

            // 处理数据（例如显示在界面上）
            Log.e("","Name: $name")

            // 可以使用 optString、optInt 等方法来避免解析错误导致的崩溃
            val address = jsonObject.optString("address")

            // 处理空字段
            if (address.isNullOrEmpty()) {
                println("No address available")
            } else {
                println("Address: $address")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val jsonString = currDes.toString().trimIndent()


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        floatView = inflater.inflate(R.layout.floating_layout,null) as ViewGroup

        btnmax = floatView.findViewById(R.id.btnMax)
        //edtDes = floatView.findViewById(R.id.edt_des2)

        //edtDes.setText(currDes) //读取存下来的json
        //edtDes.setSelection(edtDes.text.toString().length)
        //edtDes.isCursorVisible = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPR = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            LAYOUT_TYPR = WindowManager.LayoutParams.TYPE_PHONE
        }

        floatWindowLayoutParams = WindowManager.LayoutParams(
            700 ,
            600,
            LAYOUT_TYPR!!,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        floatWindowLayoutParams.gravity = Gravity.CENTER
        floatWindowLayoutParams.x = 0
        floatWindowLayoutParams.y = 0

        windowManager.addView(floatView, floatWindowLayoutParams)

        parseJson(jsonString)

        /*关闭悬浮窗*/
        btnmax.setOnClickListener {
            stopSelf()
            windowManager.removeView(floatView)
            val back = Intent(this@FloatingWindowApp,MainActivity::class.java)
            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(back)
        }
        /*拖动窗口*/
        floatView.setOnTouchListener(object : View.OnTouchListener{
            val updatedFloatingWindowLayoutParams = floatWindowLayoutParams
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event!!.action)
                {
                    MotionEvent.ACTION_DOWN->{
                        x = updatedFloatingWindowLayoutParams.x.toDouble()
                        y = updatedFloatingWindowLayoutParams.x.toDouble()
                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE->{
                        updatedFloatingWindowLayoutParams.x = (x + event.rawX - px).toInt()
                        updatedFloatingWindowLayoutParams.y = (y + event.rawY - py).toInt()
                        windowManager.updateViewLayout(floatView,updatedFloatingWindowLayoutParams)
                    }
                }
                return false
            }
        })


        /*json解析*/

    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        windowManager.removeView(floatView)
    }
}