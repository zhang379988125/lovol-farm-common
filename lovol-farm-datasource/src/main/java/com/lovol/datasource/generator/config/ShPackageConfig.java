package com.lovol.datasource.generator.config;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ShPackageConfig extends PackageConfig {

    /**
     * Dto包名
     */
    private String dto = "dto";
    /**
     * Bo包名
     */
    private String bo = "bo";

    private String metaObjectHandler = "handler.mybatis";


}

