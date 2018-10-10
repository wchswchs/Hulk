package com.mtl.hulk;

import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.db.HulkDataSource;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractHulkTest {

    @Autowired
    protected HulkProperties properties;
    @Autowired
    protected HulkDataSource hulkDataSource;

}
