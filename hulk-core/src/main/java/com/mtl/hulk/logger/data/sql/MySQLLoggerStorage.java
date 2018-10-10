package com.mtl.hulk.logger.data.sql;

import com.alibaba.fastjson.JSONObject;
import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.db.HulkDataSource;
import com.mtl.hulk.HulkSerializer;
import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@SuppressWarnings("all")
public class MySQLLoggerStorage extends BusinessActivityLogger {

    private final Logger logger = LoggerFactory.getLogger(MySQLLoggerStorage.class);

    public MySQLLoggerStorage(HulkDataSource dataSource, HulkSerializer serializer) {
        super(dataSource, serializer);
    }

    @Override
    public boolean write(RuntimeContext context, BusinessActivityContext businessActivityContext) throws SQLException {
        boolean businessActivityLog = writeBusinessActivityLog(context, businessActivityContext);
        if (businessActivityLog == true) {
            return true;
        }
        return false;
    }

    private boolean writeBusinessActivityLog(RuntimeContext context, BusinessActivityContext businessActivityContext) throws SQLException {
        if (null == context) {
            return false;
        }

        String sql1 = "INSERT INTO tm_business_activity_log(businessActivityId,businessActivityStatus,startTime,runtimeContext,businessActivityContext) " +
                "VALUES (?, ?, ?,?,?)";
        try (PreparedStatement ptmt = dataSource.prepareStatement(sql1)) {
            ptmt.setString(1, getBusinessActivityIdStr(context.getActivity().getId()));
            ptmt.setInt(2, context.getActivity().getStatus().getCode());
            ptmt.setDate(3, new Date(context.getActivity().getStartTime().getTime()));
            ptmt.setString(4, JSONObject.toJSONString(context));
            ptmt.setString(5, JSONObject.toJSONString(businessActivityContext));
            if (ptmt.executeUpdate() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }


    @Override
    public List<HulkTransactionActivity> read(int size) throws SQLException {
        String sql = "SELECT businessActivityId,businessActivityStatus,startTime,runtimeContext,businessActivityContext " +
                "FROM tm_business_activity_log " +
                "WHERE isDeleted = 0 and businessActivityStatus in ('8', '9') limit ?";
        try (PreparedStatement ptmt = dataSource.prepareStatement(sql)) {
            ptmt.setInt(1, size);
            ResultSet resultSet = ptmt.executeQuery();
            List<HulkTransactionActivity> hulkTransactionActivityList = new ArrayList<>();
            while (resultSet.next()) {
                BusinessActivity businessActivity = new BusinessActivity();
                HulkContext hulkContext = new HulkContext();
                HulkTransactionActivity hulkTransactionActivity = new HulkTransactionActivity();

                businessActivity.setId(getBusinessActivityId(resultSet.getString("businessActivityId")));
                businessActivity.setStartTime(resultSet.getDate("startTime"));
                businessActivity.setStatus(BusinessActivityStatus.getBusinessActivityStatus(resultSet.getInt("businessActivityStatus")));

                RuntimeContext runtimeContext = JSONObject.parseObject(resultSet.getString("runtimeContext"), RuntimeContext.class);
                BusinessActivityContext businessActivityContext = JSONObject.parseObject(resultSet.getString("businessActivityContext"), BusinessActivityContext.class);

                hulkContext.setBac(businessActivityContext);
                hulkContext.setRc(runtimeContext);
                hulkTransactionActivity.setBusinessActivity(businessActivity);
                hulkTransactionActivity.setHulkContext(hulkContext);
                hulkTransactionActivityList.add(hulkTransactionActivity);
            }
            if (CollectionUtils.isEmpty(hulkTransactionActivityList)) {
                return null;
            }
            return hulkTransactionActivityList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    @Override
    public int remove(List<String> businessActivityIds) throws SQLException {
        String sql = "UPDATE tm_business_activity_log SET isDeleted = 1 " +
                "WHERE isDeleted = 0 and businessActivityId in (" + transListString(businessActivityIds) + ") ";
        try (PreparedStatement ptmt = dataSource.prepareStatement(sql)) {
            int index = 1;
            for (String businessActivityId : businessActivityIds) {
                ptmt.setString(index++, businessActivityId);
            }
            int rsCount = ptmt.executeUpdate();
            if (rsCount > 0) {
                return rsCount;
            }
            return 0;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public boolean writeEx(BusinessActivityException ex) throws SQLException {
        if (null == ex) {
            return false;
        }

        String sql = "INSERT INTO tm_business_activity_exception_log(businessActivityId,exceptionContent) " +
                "VALUES (?, ?)";
        try (PreparedStatement ptmt = dataSource.prepareStatement(sql)) {
            ptmt.setString(1, getBusinessActivityIdStr(ex.getId()));
            ptmt.setString(2, ex.getException());
            if (ptmt.executeUpdate() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int updateBusinessActivityState(String businessActivityIds, BusinessActivityStatus businessActivityStatus) throws SQLException {
        String sql = "UPDATE tm_business_activity_log SET businessActivityStatus = ? " +
                "WHERE isDeleted = 0 and businessActivityId = ?";
        try (PreparedStatement ptmt = dataSource.prepareStatement(sql)) {
            ptmt.setInt(1, businessActivityStatus.getCode());
            ptmt.setString(2, businessActivityIds);
            int rsCount = ptmt.executeUpdate();
            if (rsCount > 0) {
                return rsCount;
            }
            return 0;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public HulkTransactionActivity getTranactionBusinessActivity(BusinessActivityId businessActivityId) {
        String sql = "SELECT businessActivityId,businessActivityStatus,startTime,runtimeContext,businessActivityContext " +
                "FROM tm_business_activity_log " +
                "WHERE isDeleted = 0 and businessActivityStatus in ('8', '9') and businessActivityId in (?)";
        try (PreparedStatement ptmt = dataSource.prepareStatement(sql)) {
            String businessActivityIdStr = getBusinessActivityIdStr(businessActivityId);
            ptmt.setString(1, businessActivityIdStr);
            ResultSet resultSet = ptmt.executeQuery();
            HulkTransactionActivity hulkTransactionActivity = new HulkTransactionActivity();
            while (resultSet.next()) {
                BusinessActivity businessActivity = new BusinessActivity();
                HulkContext hulkContext = new HulkContext();
                businessActivity.setId(getBusinessActivityId(resultSet.getString("businessActivityId")));
                businessActivity.setStartTime(resultSet.getDate("startTime"));
                businessActivity.setStatus(BusinessActivityStatus.getBusinessActivityStatus(resultSet.getInt("businessActivityStatus")));

                RuntimeContext runtimeContext = JSONObject.parseObject(resultSet.getString("runtimeContext"), RuntimeContext.class);
                BusinessActivityContext businessActivityContext = JSONObject.parseObject(resultSet.getString("businessActivityContext"), BusinessActivityContext.class);

                hulkContext.setBac(businessActivityContext);
                hulkContext.setRc(runtimeContext);

                hulkTransactionActivity.setBusinessActivity(businessActivity);
                hulkTransactionActivity.setHulkContext(hulkContext);
            }
            if (StringUtils.isEmpty(hulkTransactionActivity)) {
                return null;
            }
            return hulkTransactionActivity;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private String transListString(List<String> businessActivityIds) {
        if (CollectionUtils.isEmpty(businessActivityIds)) {
            return null;
        }
        StringBuilder str = new StringBuilder();
        for (String businessActivityId : businessActivityIds) {
            str.append("?,");
        }
        return str.substring(0, str.length() - 1);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void destroyNow() {
    }

}
