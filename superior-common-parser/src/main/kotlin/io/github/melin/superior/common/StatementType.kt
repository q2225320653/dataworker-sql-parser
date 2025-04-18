package io.github.melin.superior.common

import java.io.Serializable

/** Created by libinsong on 2017/3/6. */
enum class StatementType : Serializable {
    CREATE_CATALOG,
    CREATE_DATABASE,
    CREATE_SCHEMA,
    CREATE_TABLE,
    CREATE_TABLE_AS_SELECT,
    CREATE_TABLE_AS_LIKE,
    CREATE_MATERIALIZED_VIEW,
    CREATE_VIEW,
    CREATE_FILE_VIEW, // spark
    CREATE_TEMP_VIEW_USING, // spark
    CREATE_FUNCTION,
    CREATE_PROCEDURE,
    DROP_CATALOG,
    DROP_DATABASE,
    DROP_SCHEMA,
    DROP_TABLE,
    DROP_VIEW,
    DROP_MATERIALIZED_VIEW,
    DROP_FUNCTION,
    DROP_SEQUENCE,
    DROP_PROCEDURE,
    TRUNCATE_TABLE,
    REFRESH_TABLE,
    EXPORT_TABLE,
    CANCEL_EXPORT,
    ANALYZE_TABLE,
    ALTER_DATABASE,
    ALTER_VIEW,
    ALTER_MATERIALIZED_VIEW,
    ALTER_TABLE,
    REPAIR_TABLE,
    COMMENT,

    // DML
    SELECT,
    DELETE,
    UPDATE,
    MERGE,
    INSERT,
    LOAD_DATA, // spark
    SHOW,
    DESC,

    // spark
    CACHE,
    UNCACHE,
    CLEAR_CACHE,

    // spark delta
    VACUUM_TABLE,
    OPTIMIZE_TABLE,
    DESC_DELTA_DETAIL,
    DESC_DELTA_HISTORY,

    // spark
    DESC_FUNCTION,
    DESC_CATALOG,
    DESC_DATABASE,
    DESC_SCHEMA,
    DESC_TABLE,
    DESC_QUERY,

    //
    REFRESH_MV,
    CANCEL_REFRESH_MV,
    EXPLAIN,
    SET,
    RESET,
    USE,
    SPARK_DIST_CP,
    DATATUNNEL, // spark
    MERGE_FILE, // spark
    APP_JAR, // spark
    CALL, // hudi
    HELP, // hudi
    ARITHMETIC,

    // StarRocks
    SR_SUBMIT_TASK,
    SR_DROP_TASK,
    SR_CREATE_ROUTINE_LOAD,
    SR_PAUSE_ROUTINE_LOAD,
    SR_RESUME_ROUTINE_LOAD,
    SR_STOP_ROUTINE_LOAD,
    SR_ALTER_ROUTINE_LOAD,
    LOAD_TABLE,
    CANCEL_LOAD_TABLE,
    ALTER_LOAD_TABLE,
    ADD_RESOURCE,
    LIST_RESOURCE,
    REMOVE_RESOURCE,
    SYNC_META,
    SYNC_TABLE,
    SYNC_DATABASE,
    UNKOWN,
}
