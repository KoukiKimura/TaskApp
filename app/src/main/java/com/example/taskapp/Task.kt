package com.example.taskapp

import android.icu.util.ULocale
import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*
import java.io.Serializable

open class Task() : RealmObject(), Serializable {

    var title: String = "" //タイトル
    var contents: String= "" //内容
    var date: Date = Date() // 日時
    var category: String = "" // カテゴリ

    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0

}