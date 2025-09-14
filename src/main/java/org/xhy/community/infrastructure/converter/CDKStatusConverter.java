package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CDKStatusConverter extends BaseTypeHandler<CDKStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CDKStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public CDKStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : CDKStatus.valueOf(value);
    }

    @Override
    public CDKStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : CDKStatus.valueOf(value);
    }

    @Override
    public CDKStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : CDKStatus.valueOf(value);
    }
}