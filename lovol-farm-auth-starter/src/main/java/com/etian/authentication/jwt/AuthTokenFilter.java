package com.etian.authentication.jwt;

import com.etian.authentication.bean.BodyReaderHttpServletRequestWrapper;
import com.etian.authentication.bean.ResponseWrapper;
import com.etian.authentication.bean.TokenUser;
import com.etian.authentication.bean.UserDetailsImpl;
import com.etian.authentication.service.JwtService;
import com.etian.authentication.service.UserDetailsServiceImpl;
import com.etian.authentication.service.UserPermissionService;
import com.etian.authentication.session.AuthSessionManager;
import com.etian.authentication.util.AuthConstant;
import com.etian.common.ErrCode;
import com.etian.common.EtianConstant;
import com.etian.common.JsonUtil;
import com.etian.common.http.Header;
import com.etian.common.http.RequestBean;
import com.etian.entity.common.enums.PrivilegeEnum;
import com.etian.entity.userservice.AuthErrCodeBean;
import com.etian.entity.userservice.CapabilityEntity;
import com.etian.entity.userservice.UserEntity;
import com.etian.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * ????????????filter??????????????????token
 *
 * @author lisuyi
 * @date 2021/12/24 14:52
 */
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${system.no.required.role.uris:/ping}")
    private String noRoleUris;

    @Autowired
    private UserPermissionService permissionService;

    @Resource(name="redisTemplate")
    private RedisTemplate redisTemplate;

    public static final  String errorMessage = "errorMessage";
    public static final  String refreshToken = "refreshToken";

    /**
     * ?????????????????????
     */
    public static final Integer ADMINISTRATOR_ROLE_ID = 1;

    /**
     * ????????????????????????
     */
    public static final Integer SYSTEM_INSTITUTION_ID = 1;
    public static final String NO_CAP_PRIVALEGE_MSG = "??????????????????????????????";
    public static final String NO_ROLE_ERR_MSG = "????????????????????????";
    public static final String CAP_DISABLE_ERR_MSG = "??????????????????????????????";
    public static final String NO_CAP_CONF_ERR_MSG = "?????????????????????";
    public static final String INST_DISABLED_ERR_MSG = "????????????????????????????????????????????????";

    public static final Set<String> commonNoRoleUris  = Sets.newHashSet("/equipmentinfo/timeInfoList","/equipmentinfo/hdTrajectory","/equipmentinfo/terminalType","/equipmentinfo/lovolTrajectory","/landservice/generateLandCode","/landservice/getLandByIds","/landservice/getLandDetails","/landservice/getLandsByCoordinateRange","/warehouseinout/getByResourceType","/warehouseBusi/listWarehouse","/supplier/querySupplierInfoList","/supplier/selectSupplierInfos","/resourceOutputDetail","/getOutputInfoPage","/warehousein/getInputInfoDetail","/cropVarieties/listCropVarieties","/queryCapabilities","/queryInstTopList","/queryInstByIds","/queryInstitutionTypes","/queryInstByConditionPage","/queryMultiInstitutionsListByToken","/queryInstitutionParent","/queryMultiInstByIds","/queryInstitutionSubjects","/queryInstitutionsListByToken","/queryInstByConditionList","/queryMenuList","/queryRolesList","/queryUserByInstId","/queryUserByIds","/queryUserByConditionPage","/queryAllUserByInstId","/getUserAndCapability","/addSystemDictionaryInfo","/updateSystemDictionaryInfo","/updateSystemDictionaryStatus","/landservice/getGrowthRecord","/landservice/soil/getHumidity","/landservice/soil/getTemperature","/landservice/soil/getFertility","/plan/getPlanDetail","/plan/getPlanList","/plan/getPlans","/standardjobs/getPlantResult","/getSystemDictionary","/getSystemDictionaryInfo","/budget/getBudgetPage","/contract/approved","/workCalculateArea/deleteById","/workCalculateArea/batchSavePoint","/workCalculateArea/getAllWorkCalculateAreas","/workCalculateArea/getWorkCalculateAreaById","/workCalculateArea/updateWorkCalculateAreaInfo","/workCalculateArea/addWorkCalculateAreaInfo","/supplier/detail","/resourceback/detail","/resourceback/getProcessInfo","/resourceorder/clearContractInfoToOrderInfo","/resourceorder/updateContractInfoToOrderInfo","/resourceorder/queryOrderProcessByNo","/warehouseinout/getReviseLogProcessInfo","/warehouseinout/getReviseLogDetail","/warehouseinout/getAllStockInfoDetail","/getSystemDictionaryInfoNoAdmin","/budget/exportBudget","/queryAppMenuList");

    private Boolean checkPrivilege(Header header,String userId,String saasId,String uri){
        Set<String> permissionUrls = permissionService.findPermissionUrls(userId,saasId);
        permissionUrls.addAll(Sets.newHashSet(noRoleUris.split(",")));
        permissionUrls.addAll(commonNoRoleUris);

        for (String string : permissionUrls) {
            if (string.contains("*")) {
                if(Pattern.compile(string).matcher(uri).find()) {
                    return true;
                }
            } else if (string.equals(uri)){
                return true;
            }
        }
        logger.info("???????????????uri:{}" + uri);
        if (header.getUa()!=null && StringUtils.equals("PC",header.getUa().getPlatform())) {
            redisTemplate.opsForSet().add("common:no:privilege:uris",uri);
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // ???post???????????????
        if (request.getRequestURI().contains(AuthConstant.UPLOADNAME) || request.getRequestURI().contains(AuthConstant.FILEUPLOADNAME) || !request.getMethod().equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }
        BodyReaderHttpServletRequestWrapper requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        String newToken = "";
        try {
            if (!requestWrapper.getRequestURI().contains(EtianConstant.PROTOCOL_LOGIN)) {
                // 1?????????request???????????????token???requestBean??????
                Header header = parseJwt(requestWrapper);
                if(header == null) {
                    request.setAttribute(errorMessage, AuthErrCodeBean.getInstance().setMessage("??????????????????token??????????????????"));
                    filterChain.doFilter(requestWrapper, response);
                    return;
                }

                // 2?????????token
                String jwt = header.getToken();
                if (jwt != null && jwtService.validateJwtToken(jwt)) {
                    //3?????????token???????????????????????????20%,??????????????????token
                    newToken = jwtService.validateExpiration(jwt);
                    UserEntity authUserEntity;
                    CapabilityEntity cap;
                    // 3??????jwt?????????????????????
                    TokenUser tokenUser = jwtService.getUserNameFromJwtToken(jwt);
                    if(tokenUser == null || tokenUser.getUserId()==null) {
                        log.error("??????????????????token??????????????????");
                        request.setAttribute(errorMessage, AuthErrCodeBean.getInstance().setMessage("??????????????????token??????????????????"));
                        filterChain.doFilter(requestWrapper, response);
                        return;
                    }
                    // 4??????jwt?????????username?????????id
                    String username = tokenUser.getUsername();
                    String institutionid = tokenUser.getInstitutionId();

                    userDetailsService.setToken(jwt);

                    // ?????????????????????????????????????????????????????????????????????????????????
                    if(requestWrapper.getRequestURI().contains(EtianConstant.PROTOCOL_GET_USER_AND_CAPABILITY)) {

                        authUserEntity = createDefaltUserEntity(username, institutionid);
                        AuthSessionManager.remove();
                    } else{
                        authUserEntity  = permissionService.findUserEntity(tokenUser.getUserId());

                        String innerInvoke = request.getHeader(AuthConstant.INNER_INVOKE);
                        if (!Boolean.TRUE.toString().equals(innerInvoke)) {
                            Boolean flag = checkPrivilege(header,tokenUser.getUserId(),tokenUser.getSaasId(),request.getRequestURI());
                            if (!flag) {
                                request.setAttribute(errorMessage, AuthErrCodeBean.getInstance().setCode(ErrCode.ERR_LOGIN_DISABLE_CAP_ERROR).setMessage(NO_CAP_PRIVALEGE_MSG).setExtendMessage(request.getRequestURI()));
                                filterChain.doFilter(requestWrapper, response);
                                return;
                            }
                        }
                        AuthSessionManager.set(authUserEntity);
                    }

                    if(authUserEntity != null ) {
                        // ????????????
                        setProps(header, authUserEntity, requestWrapper);
                    }
                }else{
                    //??????token???????????????????????????????????? add by wyj 2022/08/15
                    AuthSessionManager.remove();
                }
            }
        } catch (CustomException ex) {
            AuthSessionManager.remove();
            request.setAttribute(errorMessage, AuthErrCodeBean.getInstance().setMessage(ex.getErrorMessage()));
            log.error("????????????: {}, {}", request.getRequestURL(), request.getPathInfo(), ex);
        } catch (Exception e) {
            AuthSessionManager.remove();
            log.error("????????????:", e);
            log.error("????????????????????????: {}, {}", request.getRequestURL(), request.getPathInfo());
        }

        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        filterChain.doFilter(requestWrapper, responseWrapper);
        byte[] bytes = responseWrapper.getContent();
        if (StringUtils.isNotEmpty(newToken)) {
            String content=new String(bytes);
            Map map = JsonUtil.parseStringToMap(content);
            Map<String,String> header = (Map<String, String>) map.get("header");
            header.put(refreshToken,newToken);
            bytes=JsonUtil.toString(map).getBytes();
        }
        //?????????bytes???response??????????????????????????????????????????response???????????????????????????????????????????????????????????????
        responseWrapper.setContentLength(-1);
        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();

    }

    /**
     * ???req????????????????????????, Session????????????????????????????????????
     * @param header ?????????
     * @param authUserEntity ??????
     * @param request ??????
     */
    private void setProps(Header header, UserEntity authUserEntity, HttpServletRequest request) {
        UserDetailsImpl userDetails = userDetailsService.loadDefaultUser(authUserEntity);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        request.setAttribute(AuthConstant.USER_NAME, authUserEntity.getUsername());
        AuthSessionManager.setHeader(header);
        setUserPrivilege(authUserEntity);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * ??????????????????
     * @param username ?????????
     * @param institutionId ??????Id
     * @return UserEntity
     */
    private UserEntity createDefaltUserEntity(String username, String institutionId) {
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setInstitutionId(Integer.valueOf(institutionId));
        return entity;
    }

    private void setUserPrivilege(UserEntity authUserEntity) {
        // ????????????
        if(authUserEntity.getInstitutionIdArray() != null && authUserEntity.getInstitutionIdArray().size() > 0 && authUserEntity.getInstitutionIdArray().get(0) != null
                && authUserEntity.getInstitutionIdArray().get(0).equals(SYSTEM_INSTITUTION_ID)) {
            //??????
            authUserEntity.setPrivilegeLevel(PrivilegeEnum.SYSTEM_USER.getCode());
        } else {
            // ??????
            authUserEntity.setPrivilegeLevel(PrivilegeEnum.CUSTOMER.getCode());
        }
        // ?????????????????????????????????
        if(authUserEntity.getRole() != null && authUserEntity.getRole().size() > 0 && authUserEntity.getRole().get(0) != null
                && authUserEntity.getRole().get(0).getId().equals(ADMINISTRATOR_ROLE_ID) && authUserEntity.getPrivilegeLevel() >= PrivilegeEnum.SYSTEM_USER.getCode()) {
            // ????????????????????????
            authUserEntity.setPrivilegeLevel(PrivilegeEnum.ADMINISTRATOR.getCode());
            authUserEntity.setAdministrator(true);
        }
    }

    private Header parseJwt(BodyReaderHttpServletRequestWrapper requestWrapper) throws CustomException {
        try {
            String json = JsonUtil.getJsonFromReq(requestWrapper);
            ObjectMapper om = new ObjectMapper();
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            RequestBean requestBean = om.readValue(json, RequestBean.class);
            if(requestBean != null && requestBean.getHeader() != null) {
                return requestBean.getHeader();
            }
        } catch (Exception ex) {
            log.error("jwt????????????", ex);
            requestWrapper.setAttribute(AuthTokenFilter.errorMessage, "??????????????????");
        }
        return null;
    }

}