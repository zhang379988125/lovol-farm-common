package ${package.Controller};

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* <p>
  * ${table.comment!} 前端控制器
  * </p>
*
* @author ${author}
* @since ${date}
*/
@Slf4j
@Api(tags = {"${entity} API"})
@RestController
@RequestMapping(value = "/${table.entityPath}")
public class ${table.controllerName} {


}
