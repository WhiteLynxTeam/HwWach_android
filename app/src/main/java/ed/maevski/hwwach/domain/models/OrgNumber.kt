package ed.maevski.hwwach.domain.models

/**
 * Номер организации (org-roles-plan.md, 9.2): 6 случайных цифр + 7-я контрольная по алгоритму Луна,
 * как у банковских карт. Контрольная цифра ловит опечатку при диктовке ещё до запроса к серверу.
 * Та же проверка — на сервере (ts, common/org-number.ts).
 */
object OrgNumber {
    const val LENGTH = 7

    /** Только цифры из ввода («482 913 5» → «4829135»), не длиннее номера. */
    fun digits(raw: String): String = raw.filter(Char::isDigit).take(LENGTH)

    /** Номер верный: 7 цифр, первая не 0, контрольная сходится. */
    fun isValid(raw: String): Boolean {
        val d = digits(raw)
        return d.length == LENGTH && d[0] != '0' && checkDigit(d.substring(0, LENGTH - 1)) == d[LENGTH - 1] - '0'
    }

    /** «4829135» → «482 913 5». */
    fun format(number: String): String {
        val d = digits(number)
        return if (d.length == LENGTH) "${d.substring(0, 3)} ${d.substring(3, 6)} ${d.substring(6)}" else number
    }

    // Справа налево удваивается каждая вторая цифра, начиная с крайней; больше 9 — минус 9;
    // контрольная дополняет сумму до кратного 10
    private fun checkDigit(payload: String): Int {
        var sum = 0
        for (i in payload.indices) {
            var d = payload[payload.length - 1 - i] - '0'
            if (i % 2 == 0) {
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
        }
        return (10 - sum % 10) % 10
    }
}
