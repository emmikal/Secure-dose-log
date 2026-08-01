package com.example.turboautismdoselog

/**
 * Escapes a single CSV field per RFC 4180: wraps in quotes and doubles
 * any internal quotes if the field contains a comma, quote, or newline.
 */
fun csvEscape(field: String?): String {
    val value = field ?: ""
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        "\"" + value.replace("\"", "\"\"") + "\""
    } else {
        value
    }
}

/**
 * Parses a single CSV line, respecting quoted fields that may contain
 * commas, escaped quotes ("" inside a quoted field), or newlines.
 */
fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0

    while (i < line.length) {
        val c = line[i]
        if (inQuotes) {
            if (c == '"') {
                if (i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = false
                }
            } else {
                sb.append(c)
            }
        } else {
            when (c) {
                '"' -> inQuotes = true
                ',' -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        i++
    }
    result.add(sb.toString())
    return result
}