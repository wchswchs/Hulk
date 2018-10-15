package com.mtl.hulk.bam;

public interface BusinessActivityManager {

    boolean commit() throws Exception;

    boolean rollback() throws Exception;

}
