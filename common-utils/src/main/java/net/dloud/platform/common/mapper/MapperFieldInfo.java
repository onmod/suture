package net.dloud.platform.common.mapper;

/**
 * @author QuDasheng
 * @create 2018-09-14 10:12
 **/
public class MapperFieldInfo {
    private String fieldName;

    private String paramName;

    private String columnName;

    public MapperFieldInfo(String fieldName, String paramName, String columnName) {
        this.fieldName = fieldName;
        this.paramName = paramName;
        this.columnName = columnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getParamName() {
        return paramName;
    }

    public String getColumnName() {
        return columnName;
    }
}
