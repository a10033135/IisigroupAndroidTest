package idv.fan.iisigroup.android.test.core

/**
 * 格式化數字：整數不顯示小數點，小數去除尾部的零。
 * 例：1.0 → "1"，3.1400 → "3.14"，0.0001 → "0.0001"
 */
fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else "%.4f".format(value).trimEnd('0').trimEnd('.')
