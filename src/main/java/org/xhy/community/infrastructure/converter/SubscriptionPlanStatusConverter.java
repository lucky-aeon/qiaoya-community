package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SubscriptionPlanStatusConverter extends BaseTypeHandler<SubscriptionPlanStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SubscriptionPlanStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public SubscriptionPlanStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String status = rs.getString(columnName);
        return status == null ? null : SubscriptionPlanStatus.valueOf(status);
    }

    @Override
    public SubscriptionPlanStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String status = rs.getString(columnIndex);
        return status == null ? null : SubscriptionPlanStatus.valueOf(status);
    }

    @Override
    public SubscriptionPlanStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String status = cs.getString(columnIndex);
        return status == null ? null : SubscriptionPlanStatus.valueOf(status);
    }
}