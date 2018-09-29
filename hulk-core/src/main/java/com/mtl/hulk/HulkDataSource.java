package com.mtl.hulk;

import org.apache.tomcat.jdbc.pool.DataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class HulkDataSource implements Resource {

    protected final List<DataSource> writeDataSources;
    protected final List<DataSource> readDataSources;

    public HulkDataSource(List<DataSource> writeDataSources, List<DataSource> readDataSources) {
        this.writeDataSources = writeDataSources;
        this.readDataSources = readDataSources;
    }

    public abstract PreparedStatement prepareStatement(String sql) throws SQLException;

    @Override
    public void destroy() {
        for (DataSource ds : writeDataSources) {
            ds.close();
        }
        for (DataSource ds : readDataSources) {
            ds.close();
        }
    }

    @Override
    public void destroyNow() {
    }

}
