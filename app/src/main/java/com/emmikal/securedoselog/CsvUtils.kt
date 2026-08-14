package com.emmikal.securedoselog

/**
 * Escapes a single CSV field per RFC 4180 and prevents spreadsheet formula
 * injection when the exported CSV is opened by spreadsheet software.
 *
 * Values beginning with '=', '+', '-', or '@' are prefixed with an apostrophe
 * so that spreadsheet applications treat them as text rather than formulas.
 */
fun csvEscape(field: String?): String {
    val value = field ?: ""

    val safeValue = if (
        value.startsWith("=") ||
        value.startsWith("+") ||
        value.startsWith("-") ||
        value.startsWith("@")
    ) {
        "'$value"
    } else {
        value
    }

    return if (
        safeValue.contains(",") ||
        safeValue.contains("\"") ||
        safeValue.contains("\n") ||
        safeValue.contains("\r")
    ) {
        "\"" + safeValue.replace("\"", "\"\"") + "\""
    } else {
        safeValue
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