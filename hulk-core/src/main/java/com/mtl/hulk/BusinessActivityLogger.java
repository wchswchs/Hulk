package com.mtl.hulk;

import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.model.*;
import com.mtl.hulk.context.RuntimeContext;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public abstract class BusinessActivityLogger extends AbstractHulk {

    protected final HulkSerializer serializer;
    protected HulkDataSource dataSource;

    public BusinessActivityLogger(HulkDataSource ds, HulkSerializer serializer) {
        this.dataSource = ds;
        this.serializer = serializer;
    }

    public abstract boolean write(RuntimeContext context, BusinessActivityContext businessActivityContext) throws SQLException;

    public abstract List<HulkTransactionActivity> read(int size) throws SQLException;

    public abstract int remove(List<String> businessActivityIds) throws SQLException;

    public abstract boolean writeEx(BusinessActivityException ex) throws SQLException;

    public abstract int updateBusinessActivityState(String businessActivityId, BusinessActivityStatus businessActivityStatus) throws SQLException;

    public abstract HulkTransactionActivity getTranactionBusinessActivity(BusinessActivityId businessActivityId);

    public static String getBusinessActivityIdStr(BusinessActivityId businessActivityId) {
        if (null == businessActivityId) {
            return "";
        }
        String businessDomain = StringUtils.isBlank(businessActivityId.getBusinessDomain()) ? " " : businessActivityId.getBusinessDomain();
        String businessActivity = StringUtils.isBlank(businessActivityId.getBusinessActivity()) ? " " : businessActivityId.getBusinessActivity();
        String entityId = StringUtils.isBlank(businessActivityId.getEntityId()) ? " " : businessActivityId.getEntityId();
        String sequence = StringUtils.isBlank(businessActivityId.getSequence()) ? " " : businessActivityId.getSequence();
        return sequence + "_" + businessDomain + "_" + businessActivity + "_" + entityId;
    }

    public BusinessActivityId getBusinessActivityId(String businessActivityIdStr) {
        BusinessActivityId businessActivityId = new BusinessActivityId();
        if (StringUtils.isBlank(businessActivityIdStr) || !businessActivityIdStr.contains("_")) {
            return businessActivityId;
        }
        String[] businessActivityIdStrs = businessActivityIdStr.split("_");
        businessActivityId.setSequence(StringUtils.isBlank(businessActivityIdStrs[0]) ? null : businessActivityIdStrs[0]);
        businessActivityId.setBusinessDomain(StringUtils.isBlank(businessActivityIdStrs[1]) ? null : businessActivityIdStrs[1]);
        businessActivityId.setBusinessActivity(StringUtils.isBlank(businessActivityIdStrs[2]) ? null : businessActivityIdStrs[2]);
        businessActivityId.setEntityId(StringUtils.isBlank(businessActivityIdStrs[3]) ? null : businessActivityIdStrs[3]);

        return businessActivityId;
    }

    public void setDataSource(HulkDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HulkDataSource getDataSource() {
        return dataSource;
    }

}
