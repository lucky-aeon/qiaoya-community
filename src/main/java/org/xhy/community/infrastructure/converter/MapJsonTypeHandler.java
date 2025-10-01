package org.xhy.community.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
@MappedTypes(Map.class)
public class MapJsonTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setObject(i, json, java.sql.Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting Map<String, Object> to JSON", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJsonToMap(json);
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJsonToMap(json);
    }

    private Map<String, Object> parseJsonToMap(String json) throws SQLException {
        if (json == null) {
            return null;
        }
        String trimmed = json.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }

        try {
            // 优先按对象解析
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {});
            }

            // 非对象（数字/字符串/布尔/数组等）场景，做兼容包装
            Object value = objectMapper.readValue(trimmed, Object.class);
            return java.util.Map.of("value", value);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to Map<String, Object>: " + json, e);
        }
    }
}
