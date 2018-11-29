package com.eddc.jnj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties
@PropertySource(value = "classpath:config/export_path_config.properties", encoding = "UTF-8")
public class Export_path_config {

    private String guangdong_basicPath;
    private String guangdong_zipBasicPath;
    private String siChuan_basicPath;
    private String siChuan_zipBasicPath;
    private String heNan_basicPath;
    private String heNan_zipBasicPath;
    private String shanxi_basicPath;

    public String getShanxi_basicPath() {
        return shanxi_basicPath;
    }

    public void setShanxi_basicPath(String shanxi_basicPath) {
        this.shanxi_basicPath = shanxi_basicPath;
    }

    public String getGuangdong_basicPath() {
        return guangdong_basicPath;
    }

    public void setGuangdong_basicPath(String guangdong_basicPath) {
        this.guangdong_basicPath = guangdong_basicPath;
    }

    public String getGuangdong_zipBasicPath() {
        return guangdong_zipBasicPath;
    }

    public void setGuangdong_zipBasicPath(String guangdong_zipBasicPath) {
        this.guangdong_zipBasicPath = guangdong_zipBasicPath;
    }

    public String getSiChuan_basicPath() {
        return siChuan_basicPath;
    }

    public void setSiChuan_basicPath(String siChuan_basicPath) {
        this.siChuan_basicPath = siChuan_basicPath;
    }

    public String getSiChuan_zipBasicPath() {
        return siChuan_zipBasicPath;
    }

    public void setSiChuan_zipBasicPath(String siChuan_zipBasicPath) {
        this.siChuan_zipBasicPath = siChuan_zipBasicPath;
    }

    public String getHeNan_basicPath() {
        return heNan_basicPath;
    }

    public void setHeNan_basicPath(String heNan_basicPath) {
        this.heNan_basicPath = heNan_basicPath;
    }

    public String getHeNan_zipBasicPath() {
        return heNan_zipBasicPath;
    }

    public void setHeNan_zipBasicPath(String heNan_zipBasicPath) {
        this.heNan_zipBasicPath = heNan_zipBasicPath;
    }
}
