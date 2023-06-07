package io.github.melin.superior.parser.flink

import com.github.melin.superior.sql.parser.util.CommonUtils
import io.github.melin.superior.common.*
import io.github.melin.superior.common.relational.DefaultStatement
import io.github.melin.superior.common.relational.FunctionId
import io.github.melin.superior.common.relational.Statement
import io.github.melin.superior.common.relational.TableId
import io.github.melin.superior.common.relational.common.ShowStatement
import io.github.melin.superior.common.relational.create.CreateTable
import io.github.melin.superior.common.relational.create.CreateTableAsSelect
import io.github.melin.superior.common.relational.table.ColumnDefType
import io.github.melin.superior.common.relational.table.ColumnRel
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParser
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParser.ComputedColumnDefinitionContext
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParser.MetadataColumnDefinitionContext
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParser.PhysicalColumnDefinitionContext
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParser.SourceTableContext
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParser.TablePropertyContext
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParser.TablePropertyListContext
import io.github.melin.superior.parser.flink.antlr4.FlinkSqlParserBaseVisitor
import org.antlr.v4.runtime.tree.RuleNode
import org.apache.commons.lang3.StringUtils

/**
 *
 * Created by libinsong on 2018/1/10.
 */
class FlinkSqlAntlr4Visitor(val splitSql: Boolean = false): FlinkSqlParserBaseVisitor<Statement>() {
    private var currentOptType: StatementType = StatementType.UNKOWN
    private var currentAlterType: AlterType = AlterType.UNKOWN
    private var multiInsertToken: String? = null

    private var limit: Int? = null
    private var offset: Int? = null
    private var inputTables: ArrayList<TableId> = arrayListOf()
    private var outputTables: ArrayList<TableId> = arrayListOf()
    private var cteTempTables: ArrayList<TableId> = arrayListOf()
    private var functionNames: HashSet<FunctionId> = hashSetOf()

    private var command: String? = null

    private var statements: ArrayList<Statement> = arrayListOf()
    private val sqls: ArrayList<String> = arrayListOf()

    fun getSqlStatements(): List<Statement> {
        return statements
    }

    fun getSplitSqls(): List<String> {
        return sqls
    }

    fun setCommand(command: String) {
        this.command = command
    }

    override fun shouldVisitNextChild(node: RuleNode, currentResult: Statement?): Boolean {
        return if (currentResult == null) true else false
    }

    override fun visitSqlStatements(ctx: FlinkSqlParser.SqlStatementsContext): Statement? {
        ctx.sqlStatement().forEach {
            var sql = StringUtils.substring(command, it.start.startIndex, it.stop.stopIndex + 1)
            sql = CommonUtils.cleanLastSemi(sql)
            if (splitSql) {
                sqls.add(sql)
            } else {
                val startNode = it.start.text
                val statement = if (StringUtils.equalsIgnoreCase("show", startNode)) {
                    val keyWords: ArrayList<String> = arrayListOf()
                    CommonUtils.findNodes(keyWords, it)
                    ShowStatement(*keyWords.toTypedArray())
                } else {
                    var statement = this.visitSqlStatement(it)
                    if (statement == null) {
                        statement = DefaultStatement(StatementType.UNKOWN)
                    }
                    statement
                }

                statement.setSql(sql)
                statements.add(statement)

                clean()
            }
        }
        return null
    }

    private fun clean() {
        currentOptType = StatementType.UNKOWN

        limit = null
        offset = null
        inputTables = arrayListOf()
        outputTables = arrayListOf()
        cteTempTables = arrayListOf()
        functionNames = hashSetOf()
    }

    override fun visitSqlStatement(ctx: FlinkSqlParser.SqlStatementContext): Statement? {
        val statement = super.visitSqlStatement(ctx)

        if (statement == null) {
            val startToken = StringUtils.lowerCase(ctx.getStart().text)
            if ("desc".equals(startToken) || "describe".equals(startToken)) {
                return DefaultStatement(StatementType.DESC)
            } else {
                throw SQLParserException("不支持的SQL: " + command)
            }
        }
        return statement
    }

    override fun visitSimpleCreateTable(ctx: FlinkSqlParser.SimpleCreateTableContext): Statement {
        val tableId = parseSourceTable(ctx.sourceTable())
        val comment: String? = if (ctx.commentSpec() != null) ctx.commentSpec().STRING_LITERAL().text else null;
        val properties = parseTableOptions(ctx.withOption().tablePropertyList())

        val columnRels = ctx.columnOptionDefinition().map {
            val column = it.getChild(0)
            if (column is PhysicalColumnDefinitionContext) {
                val colName = column.columnName().text
                val dataType = column.columnType().text
                val colComment: String? = if (column.commentSpec() != null) column.commentSpec().STRING_LITERAL().text else null
                ColumnRel(colName, dataType, colComment, ColumnDefType.PHYSICAL)
            } else if (column is MetadataColumnDefinitionContext) {
                val colName = column.columnName().text
                val dataType = column.columnType().text
                ColumnRel(colName, dataType, null, ColumnDefType.METADATA)
            } else {
                val computedColumn = column as ComputedColumnDefinitionContext
                val colName = computedColumn.columnName().text
                val colComment: String? = if (computedColumn.commentSpec() != null) computedColumn.commentSpec().STRING_LITERAL().text else null
                ColumnRel(colName, null, colComment, ColumnDefType.COMPUTED)
            }
        }

        return CreateTable(tableId, comment, columnRels, properties)
    }

    private fun parseSourceTable(source: SourceTableContext): TableId {
        val nodes = source.uid().identifier()
        if (nodes.size == 3) {
            val catalog = CommonUtils.cleanQuote(nodes.get(0).text)
            val schema = CommonUtils.cleanQuote(nodes.get(1).text)
            val tableName = CommonUtils.cleanQuote(nodes.get(2).text)
            return TableId(catalog, schema, tableName)
        } else if (nodes.size == 2) {
            val schema = CommonUtils.cleanQuote(nodes.get(0).text)
            val tableName = CommonUtils.cleanQuote(nodes.get(1).text)
            return TableId(schema, tableName)
        } else if (nodes.size == 1) {
            val tableName = CommonUtils.cleanQuote(nodes.get(0).text)
            return TableId(tableName)
        } else {
            throw SQLParserException("parse multipart error: " + nodes.size)
        }
    }

    private fun parseTableOptions(ctx: TablePropertyListContext): Map<String, String> {
        val properties = HashMap<String, String>()
        if (ctx != null) {
            ctx.tableProperty().forEach { item ->
                val property = item as TablePropertyContext
                val key = CommonUtils.cleanQuote(property.key.text)
                val value = CommonUtils.cleanQuote(property.value.text)
                properties.put(key, value)
            }
        }

        return properties
    }
}
