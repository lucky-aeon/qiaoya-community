package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.updatelog.valueobject.UpdateLogStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(UpdateLogStatus.class)
public class UpdateLogStatusConverter extends BaseTypeHandler<UpdateLogStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UpdateLogStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public UpdateLogStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : UpdateLogStatus.fromCode(value);
    }

    @Override
    public UpdateLogStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : UpdateLogStatus.fromCode(value);
    }

    @Override
    public UpdateLogStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : UpdateLogStatus.fromCode(value);
    }
}