package org.xhy.community.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 List 转换器
 * 通过反射动态获取字段的泛型类型进行序列化/反序列化
 * 自动扫描实体类并缓存字段的泛型类型信息
 */
@Component
public class UniversalListConverter extends BaseTypeHandler<List<?>> {

    private static final Logger log = LoggerFactory.getLogger(UniversalListConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 无需注入 ApplicationContext

    // 缓存字段名到元素类型的映射，避免频繁反射
    private static final Map<String, Class<?>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    // 缓存实体类信息，避免重复扫描
    private static final Set<Class<?>> SCANNED_ENTITIES = new HashSet<>();

    private static volatile boolean initialized = false;

    @PostConstruct
    public void init() {
        if (!initialized) {
            synchronized (UniversalListConverter.class) {
                if (!initialized) {
                    scanEntityClasses();
                    initialized = true;
                }
            }
        }
    }

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
            // 从缓存获取元素类型
            Class<?> elementType = getElementTypeFromCache(columnName);

            if (elementType == String.class) {
                List<String> result = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                return result;
            } else {
                // 动态创建TypeReference
                List<?> result = objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
                return result;
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
            // 基于内容简单判断，默认为String列表
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON by content: {}", json, e);
            throw new SQLException("Error parsing JSON to List", e);
        }
    }

    private Class<?> getElementTypeFromCache(String columnName) {
        return FIELD_TYPE_CACHE.computeIfAbsent(columnName, this::findElementTypeByColumnName);
    }

    private Class<?> findElementTypeByColumnName(String columnName) {
        // 将数据库列名转换为Java字段名
        String fieldName = convertColumnNameToFieldName(columnName);

        // 遍历所有已扫描的实体类，查找匹配的字段
        for (Class<?> entityClass : SCANNED_ENTITIES) {
            try {
                Field field = entityClass.getDeclaredField(fieldName);
                Class<?> elementType = getListElementType(field);
                if (elementType != null) {
                    // 仅缓存类型信息，避免调试日志噪音
                    return elementType;
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在于该实体类，继续尝试下一个
            }
        }

        log.warn("Could not find field type for column {}, defaulting to String", columnName);
        return String.class;
    }

    /**
     * 通用的下划线转驼峰命名转换
     */
    private String convertColumnNameToFieldName(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return columnName;
        }

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

    /**
     * 扫描实体类并缓存字段信息
     */
    private void scanEntityClasses() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

            // 扫描 domain 包下的所有实体类
            String pattern = "classpath*:org/xhy/community/domain/**/entity/*.class";
            Resource[] resources = resolver.getResources(pattern);

            for (Resource resource : resources) {
                try {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();

                    // 只处理Entity结尾的类
                    if (className.endsWith("Entity")) {
                        Class<?> entityClass = ClassUtils.forName(className, this.getClass().getClassLoader());
                        SCANNED_ENTITIES.add(entityClass);
                    }
                } catch (Exception e) {
                    log.warn("Failed to scan entity class: {}", resource.getFilename(), e);
                }
            }

            // 扫描完成，无需输出统计日志
        } catch (Exception e) {
            log.error("Failed to scan entity classes", e);
        }
    }

    /**
     * 通过反射获取List字段的元素类型
     */
    private Class<?> getListElementType(Field field) {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type rawType = parameterizedType.getRawType();

            // 检查是否为List类型
            if (rawType instanceof Class && List.class.isAssignableFrom((Class<?>) rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
                    return (Class<?>) actualTypeArguments[0];
                }
            }
        }

        return null;
    }
}
