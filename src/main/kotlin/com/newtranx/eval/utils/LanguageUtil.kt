package com.newtranx.eval.utils

import java.util.*

/**
 * @Author  Anson
 * @Date  2022/2/25 11:17
 * @Version 1.0
 */
class LanguageUtil {
    companion object {
        var languages: Map<String, String>

        init {
            Locale.setDefault(Locale.ENGLISH)
            languages = Locale.getAvailableLocales().fold(mutableMapOf()) { acc, locale ->
                if (locale.toString().toLowerCase().isNotBlank()) {
                    val keys = locale.toString().toLowerCase().split("_")
                    if (keys.size > 1) {
                        acc[keys[0]] = locale.displayLanguage
                        acc[keys[1]] = locale.displayLanguage
                    }
                    acc[locale.toString().toLowerCase()] = locale.displayLanguage
                }
                acc
            }
        }

        fun check(str: String): Boolean {
            val lang = str.toLowerCase()
            if (languages.containsKey(lang)) {
                return true
            }
            if (languages.containsValue(lang)) {
                return true
            }
            throw Exception("Language: $str is not exist!")
        }

        fun displayLanguage(lang: String): String {
            check(lang)
            val key = lang.toLowerCase()
            return if (languages.containsKey(key)) {
                languages[key]!!
            } else {
                lang
            }
        }

        fun isAsian(lang: String) = when (lang) {
            "Chinese", "Korean", "Japanese", "Thai", "Vietnamese" -> true
            else -> false
        }
    }
}