package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.domain.cdk.valueobject.CDKType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CDKTypeConverter extends BaseTypeHandler<CDKType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CDKType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public CDKType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : CDKType.valueOf(value);
    }

    @Override
    public CDKType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : CDKType.valueOf(value);
    }

    @Override
    public CDKType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : CDKType.valueOf(value);
    }
}