package com.mtl.hulk.db;

import com.mtl.hulk.db.HulkDataSource;
import com.mtl.hulk.tools.StringUtil;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class SQLDataSource extends HulkDataSource {

    private static final Pattern SELECT_FOR_UPDATE_PATTERN = Pattern.compile(
                    "^select\\s+.*\\s+for\\s+update.*$", Pattern.CASE_INSENSITIVE);

    public SQLDataSource(List<DataSource> writeDataSources, List<DataSource> readDataSources) {
        super(writeDataSources, readDataSources);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        SqlType sqlType = getSqlType(sql);
        if (sqlType == SqlType.SELECT || sqlType == SqlType.SELECT_FOR_UPDATE) {
            return readDataSources.get((int) Math.random() * readDataSources.size())
                    .getConnection().prepareStatement(sql);
        } else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE
                || sqlType == SqlType.DELETE) {
            return writeDataSources.get((int) Math.random() * writeDataSources.size())
                    .getConnection().prepareStatement(sql);
        } else {
            throw new SQLException("only select, insert, update, delete sql is supported");
        }
    }

    private static SqlType getSqlType(String sql) throws SQLException {
        SqlType sqlType = null;
        String noCommentsSql = StringUtil.stripComments(sql, "'\"", "'\"", true, false, true,
                true).trim();

        if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
            if (SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
                sqlType = SqlType.SELECT_FOR_UPDATE;
            } else {
                sqlType = SqlType.SELECT;
            }
        } else if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "insert")) {
            sqlType = SqlType.INSERT;
        } else if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "update")) {
            sqlType = SqlType.UPDATE;
        } else if (StringUtil.startsWithIgnoreCaseAndWs(noCommentsSql, "delete")) {
            sqlType = SqlType.DELETE;
        } else {
            throw new SQLException("only select, insert, update, delete sql is supported");
        }

        return sqlType;
    }

}
