package ${cfg.bo};

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
<#if swagger2>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
/**
 <#if (table.comment??)>
 * ${table.comment}
 <#else>
 * ${entity}BO
 </#if>
 *
 * @author ${author}
 * @since ${date}
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
<#if superEntityClass??>
public class ${entity}BO extends ${superEntityClass}<#if activeRecord><${entity}></#if> {
<#elseif activeRecord>
public class ${entity}BO extends Model<${entity}> {
<#else>
public class ${entity}BO implements Serializable {
</#if>

<#if entitySerialVersionUID>
  private static final long serialVersionUID = 1L;
</#if>

  private Integer id;
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>

    <#if field.comment!?length gt 0>
      <#if swagger2>
  @ApiModelProperty(value = "${field.comment}")
      <#else>
  /**
   * ${field.comment}
   */
      </#if>
    </#if>
  private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->

  public interface Insert {
  }

  public interface Update {
  }

}






