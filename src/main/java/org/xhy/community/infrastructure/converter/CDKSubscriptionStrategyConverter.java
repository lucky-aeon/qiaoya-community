package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.cdk.valueobject.CDKSubscriptionStrategy;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CDKSubscriptionStrategyConverter extends BaseTypeHandler<CDKSubscriptionStrategy> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CDKSubscriptionStrategy parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public CDKSubscriptionStrategy getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : CDKSubscriptionStrategy.valueOf(value);
    }

    @Override
    public CDKSubscriptionStrategy getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : CDKSubscriptionStrategy.valueOf(value);
    }

    @Override
    public CDKSubscriptionStrategy getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : CDKSubscriptionStrategy.valueOf(value);
    }
}

