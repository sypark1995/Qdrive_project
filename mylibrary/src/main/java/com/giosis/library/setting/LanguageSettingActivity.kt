package com.giosis.library.setting

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.giosis.library.R

import kotlinx.android.synthetic.main.activity_language_setting1.*
import kotlinx.android.synthetic.main.top_title.*

class LanguageSettingActivity : AppCompatActivity() {

    val tag = "LanguageSettingActivity"

    // TODO_kjyoo
//    private val language = MyApplication.localeManager.language
    private val languageList = listOf(resources.getString(R.string.text_language_en),
            resources.getString(R.string.text_language_ms),
            resources.getString(R.string.text_language_id))

//    private val languageCode = listOf(LocaleManager.LANGUAGE_ENGLISH, LocaleManager.LANGUAGE_MALAY, LocaleManager.LANGUAGE_INDONESIA)

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_setting1)


        text_top_title.text = resources.getString(R.string.text_title_language_setting)


//        val adapter = LanguageAdapter(language, languageList, languageCode)
//        recycler_language.adapter = adapter

        // RecyclerView 구분선 색상 지정
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.bg_rect_ebebeb))
        recycler_language.addItemDecoration(dividerItemDecoration)

        layout_top_back.setOnClickListener {
            finish()
        }

        btn_language_setting_confirm.setOnClickListener {

//            val code = adapter.getLanguageCode()
//            Log.e(tag, " Selected  $code")


            val alertBuilder = AlertDialog.Builder(this@LanguageSettingActivity)
            alertBuilder.setMessage(resources.getString(R.string.msg_language_change_restart))
            alertBuilder.setCancelable(false)

            alertBuilder.setPositiveButton(resources.getString(R.string.button_ok)) { _, _ ->

//                MyApplication.localeManager.setNewLocale(this@LanguageSettingActivity, code)
//                MyApplication.preferences.localeLanguage = code
//
//                val intent = Intent(this@LanguageSettingActivity, MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
//
//                System.exit(0)
            }

            alertBuilder.setNegativeButton(resources.getString(R.string.button_cancel)) { _, _ ->
                finish()
            }

            alertBuilder.show()
        }
    }
}