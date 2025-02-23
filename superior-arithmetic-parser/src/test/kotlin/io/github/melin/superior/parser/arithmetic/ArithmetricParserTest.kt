package io.github.melin.superior.parser.arithmetic

import com.github.melin.superior.sql.parser.arithmetic.ArithmeticHelper
import io.github.melin.superior.common.StatementType
import org.junit.Assert
import org.junit.Test

/** Created by libinsong on 2018/1/10. */
class ArithmetricParserTest {

    @Test
    fun test0() {
        val sql =
            """
            特征1 / (特征_dd_2
                - (log2(feature_12) + 特征3))
            """

        val statement = ArithmeticHelper.parseStatement(sql, false)
        Assert.assertEquals(StatementType.ARITHMETIC, statement?.statementType)
        if (statement is ArithmeticData) {
            Assert.assertEquals(4, statement.variables.toArray().size)
            Assert.assertEquals(1, statement.functions.size)
            Assert.assertEquals("log2", statement.functions.toArray().get(0))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun test1() {
        val sql =
            """
            case when rand <= 12 then 1
                                when rand <= 23 then 2
                                else 3 end
            """

        val statement = ArithmeticHelper.parseStatement(sql, false)
        Assert.assertEquals(StatementType.ARITHMETIC, statement?.statementType)
        if (statement is ArithmeticData) {
            Assert.assertEquals(1, statement.variables.toArray().size)
            Assert.assertEquals(0, statement.functions.size)
        } else {
            Assert.fail()
        }
    }

    @Test
    fun test2() {
        val sql =
            """
            [特征1] / ([特征_dd_2]
                - (log2([feature_12]) + [特征3]))
            """

        val statement = ArithmeticHelper.parseStatement(sql)
        Assert.assertEquals(StatementType.ARITHMETIC, statement?.statementType)
        if (statement is ArithmeticData) {
            Assert.assertEquals(4, statement.variables.toArray().size)
            Assert.assertEquals(1, statement.functions.size)
            Assert.assertEquals("log2", statement.functions.toArray().get(0))
        } else {
            Assert.fail()
        }
    }
}
