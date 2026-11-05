package ed.maevski.hwwach.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Контрольная цифра номера организации должна считаться так же, как на сервере (ts, common/org-number.ts)
class OrgNumberTest {

    @Test
    fun validNumbersPass() {
        // 482913 → контрольная 1; 100000 → 9
        assertTrue(OrgNumber.isValid("4829131"))
        assertTrue(OrgNumber.isValid("482 913 1"))
        assertTrue(OrgNumber.isValid("1000009"))
    }

    @Test
    fun typosAreCaught() {
        assertFalse(OrgNumber.isValid("4829135")) // неверная контрольная
        assertFalse(OrgNumber.isValid("4829311")) // переставлены соседние 1 и 3
        assertFalse(OrgNumber.isValid("482913")) // не хватает цифры
        assertFalse(OrgNumber.isValid("0829131")) // первая цифра 0
    }

    @Test
    fun digitsAndFormat() {
        assertEquals("4829135", OrgNumber.digits("482-913 5x"))
        assertEquals("482 913 5", OrgNumber.format("4829135"))
    }
}
