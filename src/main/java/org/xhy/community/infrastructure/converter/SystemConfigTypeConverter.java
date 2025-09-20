package org.xhy.community.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.config.valueobject.SystemConfigType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(SystemConfigType.class)
public class SystemConfigTypeConverter extends BaseTypeHandler<SystemConfigType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SystemConfigType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public SystemConfigType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? SystemConfigType.valueOf(value) : null;
    }

    @Override
    public SystemConfigType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? SystemConfigType.valueOf(value) : null;
    }

    @Override
    public SystemConfigType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? SystemConfigType.valueOf(value) : null;
    }
}