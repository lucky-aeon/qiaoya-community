package org.xhy.community.infrastructure.converter;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(FavoriteTargetType.class)
public class FavoriteTargetTypeConverter extends BaseTypeHandler<FavoriteTargetType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FavoriteTargetType parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public FavoriteTargetType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : FavoriteTargetType.fromCode(value);
    }

    @Override
    public FavoriteTargetType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : FavoriteTargetType.fromCode(value);
    }

    @Override
    public FavoriteTargetType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : FavoriteTargetType.fromCode(value);
    }
}
