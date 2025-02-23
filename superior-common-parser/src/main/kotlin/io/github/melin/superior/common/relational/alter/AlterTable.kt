package io.github.melin.superior.common.relational.alter

import io.github.melin.superior.common.*
import io.github.melin.superior.common.relational.TableId
import io.github.melin.superior.common.relational.abs.AbsTableStatement
import kotlin.collections.ArrayList

data class AlterTable(override val tableId: TableId, private val action: AlterAction?) : AbsTableStatement() {
    override val statementType = StatementType.ALTER_TABLE
    override val privilegeType = PrivilegeType.ALTER
    override val sqlType = SqlType.DDL

    val actions: ArrayList<AlterAction> = ArrayList()
    var ifExists: Boolean = false

    init {
        if (action != null) {
            actions.add(action)
        }
    }

    constructor(tableId: TableId) : this(tableId, null)

    fun addAction(action: AlterAction) {
        actions.add(action)
    }

    fun addActions(list: List<AlterAction>) {
        actions.addAll(list)
    }

    fun firstAction(): AlterAction {
        return if (action != null) action else actions.first()
    }

    fun getFirstAlterType(): AlterActionType {
        val action = firstAction()
        return action.alterType
    }
}
