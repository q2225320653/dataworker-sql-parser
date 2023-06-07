package io.github.melin.superior.common.relational.table

data class ColumnRel(
    val name: String,
    val type: String? = null,
    val comment: String? = null,
    var nullable: Boolean = true,
    var defaultExpr: String? = null,
    var isPk: Boolean = false,
    val columnDefType: ColumnDefType = ColumnDefType.COMPUTED
) {
    var expression: String? = null // 计算表达式
    var position: String? = null
    var afterCol: String? = null
    var jsonPath: String? = null

    constructor(name: String, type: String?, comment: String?):
            this(name, type, comment, true, null)

    constructor(name: String, type: String?, comment: String?, columnDefType: ColumnDefType):
            this(name, type, comment, true, null, false, columnDefType)
}

enum class ColumnDefType {
    PHYSICAL,
    METADATA,
    COMPUTED
}
