package io.github.melin.superior.parser.trino

import io.github.melin.superior.common.StatementType
import io.github.melin.superior.common.relational.create.CreateTableAsSelect
import io.github.melin.superior.common.relational.dml.DeleteTable
import io.github.melin.superior.common.relational.dml.InsertTable
import io.github.melin.superior.common.relational.dml.QueryStmt
import io.github.melin.superior.common.relational.drop.DropTable
import org.junit.Assert
import org.junit.Test

/** Created by libinsong on 2018/1/10. */
class TrinoSqlParserTest {

    @Test
    fun queryTest0() {
        val sql =
            """
            select a.* from datacompute1.datacompute.dc_job a left join datacompute1.datacompute.dc_job_scheduler b on a.id=b.job_id
        """
                .trimIndent()

        val statement = TrinoSqlHelper.parseStatement(sql)
        if (statement is QueryStmt) {
            Assert.assertEquals(StatementType.SELECT, statement.statementType)
            Assert.assertEquals(2, statement.inputTables.size)
        } else {
            Assert.fail()
        }
    }

    @Test
    fun queryTest1() {
        val sql =
            """
            SELECT COUNT(app_name) AS "应用名" FROM (SELECT * FROM ops.dwd_app_to_container_wt 
            WHERE ds=date_format(CURRENT_DATE - interval '1' DAY, "%Y%m%d") ) tdbi_view
        """
                .trimIndent()

        val statement = TrinoSqlHelper.parseStatement(sql)
        if (statement is QueryStmt) {
            Assert.assertEquals(StatementType.SELECT, statement.statementType)
            Assert.assertEquals(1, statement.inputTables.size)
        } else {
            Assert.fail()
        }
    }

    @Test
    fun queryLimitTest() {
        val sql =
            """
            select * from preso_table limit 10
        """
                .trimIndent()

        val statement = TrinoSqlHelper.parseStatement(sql)
        if (statement is QueryStmt) {
            Assert.assertEquals(StatementType.SELECT, statement.statementType)
            Assert.assertEquals(1, statement.inputTables.size)
            Assert.assertEquals(10, statement.limit)
        } else {
            Assert.fail()
        }
    }

    @Test
    fun createTableSelectTest() {
        val sql =
            """
            create table dd_s_s as select * from bigdata.test_demo_test limit 1
        """
                .trimIndent()

        val statement = TrinoSqlHelper.parseStatement(sql)
        if (statement is CreateTableAsSelect) {
            Assert.assertEquals(StatementType.CREATE_TABLE_AS_SELECT, statement.statementType)
            Assert.assertEquals("dd_s_s", statement.tableId.tableName)
            Assert.assertEquals(1, statement.queryStmt.inputTables.size)
        } else {
            Assert.fail()
        }
    }

    @Test
    fun dropTableTest() {
        val sql =
            """
            drop table if exists bigdata.tdl_small_files_2
        """
                .trimIndent()

        val statement = TrinoSqlHelper.parseStatement(sql)
        if (statement is DropTable) {
            Assert.assertEquals(StatementType.DROP_TABLE, statement.statementType)
            Assert.assertEquals("bigdata", statement.tableId.schemaName)
            Assert.assertEquals("tdl_small_files_2", statement.tableId.tableName)
        } else {
            Assert.fail()
        }
    }

    @Test
    fun insertTest() {
        val sql =
            """
            insert into orders select * from new_orders;
        """
                .trimIndent()

        val statement = TrinoSqlHelper.parseStatement(sql)
        if (statement is InsertTable) {
            Assert.assertEquals(StatementType.INSERT, statement.statementType)
            Assert.assertEquals("orders", statement.tableId.tableName)
        } else {
            Assert.fail()
        }
    }

    @Test
    fun deleteTest() {
        val sql =
            """
            DELETE FROM lineitem WHERE orderkey IN (SELECT orderkey FROM orders WHERE priority = 'LOW');
        """
                .trimIndent()

        val statement = TrinoSqlHelper.parseStatement(sql)
        if (statement is DeleteTable) {
            Assert.assertEquals(StatementType.DELETE, statement.statementType)
            Assert.assertEquals("lineitem", statement.tableId.tableName)
            Assert.assertEquals(1, statement.inputTables.size)
        } else {
            Assert.fail()
        }
    }
}
