package com.lovol.datasource.generator;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.lovol.datasource.generator.config.ShPackageConfig;
import com.lovol.datasource.generator.cons.PathCons;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class MybatisCodeGenerator {

    private DataSourceConfig dataSourceConfig;

    private GlobalConfig globalConfig;

    private TemplateConfig templateConfig;

    private StrategyConfig strategyConfig;

    private ShPackageConfig packageConfig;

    private InjectionConfig injectionConfig;

    public MybatisCodeGenerator() {
        strategyConfig = new StrategyConfig()
            // 自定义实体父类
            // 自定义实体，公共字段
            .setSuperEntityColumns("id")
            // 【实体】是否为lombok模型（默认 false）
            .setEntityLombokModel(true)
            // Boolean类型字段是否移除is前缀处理
            .setEntityBooleanColumnRemoveIsPrefix(true)
            .setRestControllerStyle(true)
            .setNaming(NamingStrategy.underline_to_camel)
            // 是否生成实体时，生成字段注解
            .setEntityTableFieldAnnotationEnable(true)
        ;
        // 自定义 controller 父类
//        strategyConfig.setSuperControllerClass("");

        dataSourceConfig = new DataSourceConfig();

        packageConfig = (ShPackageConfig) new ShPackageConfig()
            .setDto("model.dto")
            .setBo("model.bo")
            .setParent("com.lovol.farm")
            .setController("controller")
            .setEntity("model.entity")
            .setMapper("mapper")
            .setService("service")
            .setServiceImpl("service.impl");

        templateConfig = new TemplateConfig()
            .setXml(null);

        globalConfig = new GlobalConfig()
            //输出目录
            .setOutputDir(getJavaPath())
            .setBaseResultMap(true)
            .setBaseColumnList(true)
            .setOpen(false)
            .setAuthor("SwiftHorse")
            //使用自定义模板覆盖默认模板
            .setFileOverride(true);

        injectionConfig = buildInjectionConfig();
    }

    private InjectionConfig buildInjectionConfig() {
        return new InjectionConfig() {
            @Override
            public void initMap() {
//        Converter<String, String> converter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
                Map<String, Object> map = new HashMap<>();
                map.put("dto", getPackageConfig().getParent() + StringPool.DOT + packageConfig.getDto());
                map.put("bo", getPackageConfig().getParent() + StringPool.DOT + packageConfig.getBo());

                // 自动填充 handler 的输出路径
                String metaObjectPackageName = getPackageConfig().getParent() + StringPool.DOT + packageConfig.getMetaObjectHandler();
                map.put("metaObjectPackage", metaObjectPackageName);

                this.setMap(map);
            }
        }.setFileOutConfigList(this.fileOutConfigList());
    }

    private List<FileOutConfig> fileOutConfigList() {
        List<FileOutConfig> fileOutConfigList = new LinkedList<>();
        fileOutConfigList.add(new FileOutConfig(
            PathCons.MAPPER_TEMPLATES_PATH) {
            // 自定义 mapper 输出文件目录
            @Override
            public String outputFile(TableInfo tableInfo) {
                return getResourcePath() + "/mapper/" + tableInfo.getEntityName() + "Mapper.xml";
            }
        });
        fileOutConfigList.add(new FileOutConfig(
            PathCons.DTO_TEMPLATES_PATH) {
            // 自定义 DTO 输出文件目录
            @Override
            public String outputFile(TableInfo tableInfo) {
                String packageName = (getPackageConfig().getParent() + StringPool.DOT + getPackageConfig().getDto() + StringPool.DOT)
                    .replaceAll("\\.", StringPool.BACK_SLASH + File.separator);
                return getJavaPath() + StringPool.SLASH + packageName + tableInfo.getEntityName() + "DTO.java";
            }
        });
        fileOutConfigList.add(new FileOutConfig(
            PathCons.BO_TEMPLATES_PATH) {
            // 自定义 BO 输出文件目录
            @Override
            public String outputFile(TableInfo tableInfo) {
                String packageName = (getPackageConfig().getParent() + StringPool.DOT + getPackageConfig().getBo() + StringPool.DOT)
                    .replaceAll("\\.", StringPool.BACK_SLASH + File.separator);
                return getJavaPath() + StringPool.SLASH + packageName + tableInfo.getEntityName() + "BO.java";
            }
        });
        return fileOutConfigList;
    }

    /**
     * 获取根目录
     */
    private String getRootPath() {
        String file = Objects.requireNonNull(this.getClass().getClassLoader().getResource(""))
            .getFile();
        return new File(file).getParentFile().getParent();
    }

    /**
     * 获取JAVA目录
     */
    private String getJavaPath() {
        String javaPath = getRootPath() + "/src/main/java";
        System.err.println(" Generator Java Path:【 " + javaPath + " 】");
        return javaPath;
    }

    /**
     * 获取Resource目录
     */
    private String getResourcePath() {
        String resourcePath = getRootPath() + "/src/main/resources";
        System.err.println(" Generator Resource Path:【 " + resourcePath + " 】");
        return resourcePath;
    }

    /**
     * 获取test目录
     */
    private String getTestPath() {
        String testPath = getRootPath() + "/src/test/java";
        System.err.println(" Generator Test Path:【 " + testPath + " 】");
        return testPath;
    }

    public void execute() {
        new AutoGenerator()
            // 默认使用freemarker作为模板引擎
            .setTemplateEngine(new FreemarkerTemplateEngine())
            // 全局配置
            .setGlobalConfig(getGlobalConfig())
            // 数据源配置
            .setDataSource(getDataSourceConfig())
            // 策略配置
            .setStrategy(getStrategyConfig())
            // 包配置
            .setPackageInfo(getPackageConfig())
            // 注入自定义配置，可以在 VM 中使用 cfg.abc 设置的值
            .setCfg(getInjectionConfig())
            .setTemplate(getTemplateConfig()).execute();

        System.err.println("Generator Success !");
    }

}

