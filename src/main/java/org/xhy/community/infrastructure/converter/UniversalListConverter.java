package org.xhy.community.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.course.valueobject.CourseResource;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 List 转换器
 * 根据字段上的 @ListElementType 注解动态确定元素类型进行序列化/反序列化
 */
@Component
public class UniversalListConverter extends BaseTypeHandler<List<?>> {

    private static final Logger log = LoggerFactory.getLogger(UniversalListConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 缓存字段名到元素类型的映射，避免频繁反射
    private static final Map<String, Class<?>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<?> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setObject(i, json, java.sql.Types.OTHER);
        } catch (JsonProcessingException e) {
            log.error("Error converting List to JSON: {}", parameter, e);
            throw new SQLException("Error converting List to JSON", e);
        }
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json, columnName);
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        // 无法通过索引获取列名，使用内容判断
        return parseJsonByContent(json);
    }

    @Override
    public List<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        // 无法通过索引获取列名，使用内容判断
        return parseJsonByContent(json);
    }

    private List<?> parseJson(String json, String columnName) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            // 尝试从缓存获取元素类型
            Class<?> elementType = getElementTypeFromCache(columnName);

            if (elementType == String.class) {
                List<String> result = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                log.debug("Successfully parsed JSON to List<String> for column {}: {} -> {}", columnName, json, result);
                return result;
            } else if (elementType == CourseResource.class) {
                List<CourseResource> result = objectMapper.readValue(json, new TypeReference<List<CourseResource>>() {});
                log.debug("Successfully parsed JSON to List<CourseResource> for column {}: {} -> {} items", columnName, json, result.size());
                return result;
            } else {
                log.warn("Unknown element type for column {}: {}, falling back to content-based parsing", columnName, elementType);
                return parseJsonByContent(json);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON for column {}: {}", columnName, json, e);
            throw new SQLException("Error parsing JSON to List", e);
        }
    }

    private List<?> parseJsonByContent(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            // 基于内容判断类型（fallback方案）
            if (json.contains("\"title\"") && json.contains("\"description\"")) {
                // 包含CourseResource的特征字段，解析为CourseResource列表
                return objectMapper.readValue(json, new TypeReference<List<CourseResource>>() {});
            } else {
                // 默认解析为String列表
                return objectMapper.readValue(json, new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON by content: {}", json, e);
            throw new SQLException("Error parsing JSON to List", e);
        }
    }

    private Class<?> getElementTypeFromCache(String columnName) {
        return FIELD_TYPE_CACHE.computeIfAbsent(columnName, this::findElementTypeByColumnName);
    }

    private Class<?> findElementTypeByColumnName(String columnName) {
        try {
            // 根据数据库列名映射到Java字段名
            String fieldName = convertColumnNameToFieldName(columnName);

            // 从CourseEntity获取字段的@ListElementType注解
            Class<?> entityClass = org.xhy.community.domain.course.entity.CourseEntity.class;
            Field field = entityClass.getDeclaredField(fieldName);

            ListElementType annotation = field.getAnnotation(ListElementType.class);
            if (annotation != null) {
                log.debug("Found @ListElementType annotation for field {}: {}", fieldName, annotation.value());
                return annotation.value();
            }
        } catch (NoSuchFieldException | SecurityException e) {
            log.warn("Could not find field annotation for column {}: {}", columnName, e.getMessage());
        }

        // 默认返回String类型
        return String.class;
    }

    private String convertColumnNameToFieldName(String columnName) {
        // 将数据库下划线命名转换为Java驼峰命名
        switch (columnName) {
            case "tech_stack":
                return "techStack";
            case "tags":
                return "tags";
            case "resources":
                return "resources";
            default:
                // 通用转换逻辑：tech_stack -> techStack
                StringBuilder result = new StringBuilder();
                boolean capitalizeNext = false;
                for (char c : columnName.toCharArray()) {
                    if (c == '_') {
                        capitalizeNext = true;
                    } else {
                        result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                        capitalizeNext = false;
                    }
                }
                return result.toString();
        }
    }
}