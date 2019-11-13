package com.example.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.icu.util.ULocale
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.TextWatcher
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Types.NULL
import java.util.*

const val EXTRA_TASK = "com.example.taskapp.TASK"



class MainActivity : AppCompatActivity()  {
    private var category:Category = Category("")

    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override  fun onChange(element: Realm){
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        mTaskAdapter = TaskAdapter(this@MainActivity)



        listView1.setOnItemClickListener { parent, _, position, _ ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            val task = parent.adapter.getItem(position) as Task
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }
            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        searchCancel.setOnClickListener{
            Log.d("test", searchCancel.text.toString())
            if (searchCancel.text.toString() == "検索" && searchText.text.toString() != ""){
                category = Category(searchText.text.toString())
                Log.d("testCategory","test:" + category.text)
                searchCancel.setText("キャンセル")
                reloadListView()
            }else if (searchCancel.text.toString() == "キャンセル") {
                searchText.setText("")
                category = Category("")
                reloadListView()
                searchCancel.setText("検索")
            }

        }



        addTaskForTest()

        reloadListView()

    }

    private fun reloadListView() {
        val taskRealmResults: RealmResults<Task>
        if (category.text == "") {
            taskRealmResults=
                mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
        }else{
            taskRealmResults=
                mRealm.where(Task::class.java).equalTo("title",category.text).findAll().sort("date", Sort.DESCENDING)
        }

        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

    private fun addTaskForTest(){
        val task = Task()
        task.title = "作業"
        task.contents = "プログラムを書いてPUSHする"
        task.date = Date()
        task.category = "カテゴリー１"
        task.id = 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()
    }
}
