package org.xhy.community.infrastructure.converter;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.domain.resource.valueobject.ResourceType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(ResourceType.class)
public class ResourceTypeConverter extends BaseTypeHandler<ResourceType> {


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ResourceType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public ResourceType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : ResourceType.fromCode(value);
    }

    @Override
    public ResourceType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : ResourceType.fromCode(value);
    }

    @Override
    public ResourceType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : ResourceType.fromCode(value);
    }
}