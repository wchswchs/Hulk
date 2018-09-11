package com.mtl.hulk;

import org.apache.tomcat.jdbc.pool.DataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class HulkDataSource {

    protected final List<DataSource> writeDataSources;
    protected final List<DataSource> readDataSources;

    public HulkDataSource(List<DataSource> writeDataSources, List<DataSource> readDataSources) {
        this.writeDataSources = writeDataSources;
        this.readDataSources = readDataSources;
    }

    public abstract PreparedStatement prepareStatement(String sql) throws SQLException;

}
