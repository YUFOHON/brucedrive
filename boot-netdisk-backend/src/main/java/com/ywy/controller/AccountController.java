package com.ywy.controller;

import com.ywy.cache.SystemCache;
import com.ywy.core.constants.SystemConstants;
import com.ywy.core.annotation.GlobalInterceptor;
import com.ywy.core.annotation.Valid;
import com.ywy.core.base.BaseController;
import com.ywy.core.config.QQLoginConfig;
import com.ywy.core.config.SystemConfig;
import com.ywy.core.enums.VerifyRegexEnum;
import com.ywy.core.exception.BusinessException;
import com.ywy.core.vo.BaseResponseVO;
import com.ywy.pojo.dto.SessionLoginUserDto;
import com.ywy.pojo.dto.UserSpaceDto;
import com.ywy.pojo.entity.SysUser;
import com.ywy.service.SysEmailCodeService;
import com.ywy.service.SysUserService;
import com.ywy.util.CaptchaUtil;
import com.ywy.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户账号相关
 */
@Slf4j
@RestController
public class AccountController extends BaseController {
    @Resource
    private SysUserService userService;
    @Resource
    private SysEmailCodeService emailCodeService;

    @Resource
    private SystemConfig systemConfig;
    @Resource
    private QQLoginConfig qqLoginConfig;

    @Resource
    private SystemCache systemCache;

    /**
     * Get image verification code
     *
     * @param response
     * @param session
     * @param type
     * @throws IOException
     */
    @RequestMapping("getCaptcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
        // 生成图片验证码
        CaptchaUtil vCode = new CaptchaUtil(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        // 保存图片验证码值
        String code = vCode.getCode();
        // 验证码类型：0登录注册；1发送邮箱验证码
        if (type == null || type == SystemConstants.ZERO) {
            session.setAttribute(SystemConstants.SESSION_CAPTCHA_CODE_CHECK, code);
        } else {
            session.setAttribute(SystemConstants.SESSION_CAPTCHA_CODE_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }

    /**
     * Send email verification code
     *
     * @param session
     * @param email
     * @param captchaCode
     * @param type
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("sendEmailCode")
    public BaseResponseVO sendEmailCode(HttpSession session,
                                        @Valid(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                                        @Valid(required = true) String captchaCode,
                                        @Valid(required = true) Integer type) {
        try {
            // 验证图片验证码
            if (!captchaCode.equalsIgnoreCase((String) session.getAttribute(SystemConstants.SESSION_CAPTCHA_CODE_EMAIL))) {
                throw new BusinessException("Wrong captcha code");
            }
            // 发送邮箱验证码
            emailCodeService.sendEmailCode(email, type);
            return successResponse(null);
        } finally {
            // 清除图片验证码值
            session.removeAttribute(SystemConstants.SESSION_CAPTCHA_CODE_EMAIL);
        }
    }

    /**
     * 注册
     * @param session
     * @param email
     * @param nickName
     * @param password
     * @param captchaCode
     * @param emailCode
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("register")
    public BaseResponseVO register(HttpSession session,
                                   @Valid(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                                   @Valid(required = true) String nickName,
                                   @Valid(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                                   @Valid(required = true) String captchaCode,
                                   @Valid(required = true) String emailCode) {
        try {
            // 验证图片验证码
            if (!captchaCode.equalsIgnoreCase((String) session.getAttribute(SystemConstants.SESSION_CAPTCHA_CODE_CHECK))) {
                throw new BusinessException("verification code error");
            }
            // 注册
            userService.register(email, nickName, password, emailCode);
            return successResponse(null);
        } finally {
            // 清除图片验证码值
            session.removeAttribute(SystemConstants.SESSION_CAPTCHA_CODE_CHECK);
        }
    }

    /**
     * User Login
     * @param session
     * @param email
     * @param password
     * @param captchaCode
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("login")
    public BaseResponseVO login(HttpSession session,
                                   @Valid(required = true) String email,
                                   @Valid(required = true) String password,
                                   @Valid(required = true) String captchaCode) {
        try {
            // verify the image verification code
            if (!captchaCode.equalsIgnoreCase((String) session.getAttribute(SystemConstants.SESSION_CAPTCHA_CODE_CHECK))) {
                throw new BusinessException("Image verification code is incorrect");
            }
            // login
            SessionLoginUserDto loginUserDto = userService.login(email, password);
            session.setAttribute(SystemConstants.SESSION_LOGIN_USER, loginUserDto);
            return successResponse(loginUserDto);
        } finally {
            // clear the image verification code value
            session.removeAttribute(SystemConstants.SESSION_CAPTCHA_CODE_CHECK);
        }
    }

    /**
     * 重置密码
     * @param session
     * @param email
     * @param password
     * @param captchaCode
     * @param emailCode
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("resetPwd")
    public BaseResponseVO resetPwd(HttpSession session,
                                   @Valid(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                                   @Valid(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                                   @Valid(required = true) String captchaCode,
                                   @Valid(required = true) String emailCode) {
        try {
            // 验证图片验证码
            if (!captchaCode.equalsIgnoreCase((String) session.getAttribute(SystemConstants.SESSION_CAPTCHA_CODE_CHECK))) {
                throw new BusinessException("Image verification code is incorrect");
            }
            // 重置用户密码
            userService.resetPwd(email, password, emailCode);
            return successResponse(null);
        } finally {
            // 清除图片验证码值
            session.removeAttribute(SystemConstants.SESSION_CAPTCHA_CODE_CHECK);
        }
    }

    /**
     * Get user avatar
     * @param response
     * @param userId
     */
    @GlobalInterceptor(login = false)
    @RequestMapping("getAvatar/{userId}")
    public void getAvatar(HttpServletResponse response, @PathVariable("userId") String userId) {
        String avatarFolder = systemConfig.getRootPath() + SystemConstants.USER_AVATAR_FOLDER;
        File avatarFolderFile = new File(avatarFolder);
        // If the avatar directory does not exist, create it
        if (!avatarFolderFile.exists()) {
            avatarFolderFile.mkdirs();
        }
        String avatarPath = avatarFolder + userId + SystemConstants.USER_AVATAR_SUFFIX;
        File file = new File(avatarPath);
        // If the avatar does not exist, get the default avatar
        if (!file.exists()) {
            // get the default avatar
            File defaultAvatarFile= new File(systemConfig.getRootPath() + SystemConstants.USER_AVATAR_FOLDER +SystemConstants.USER_AVATAR_DEFAULT);
            if(!defaultAvatarFile.exists())  printNoDefaultImage(response);
            avatarPath = systemConfig.getRootPath() + SystemConstants.USER_AVATAR_FOLDER +SystemConstants.USER_AVATAR_DEFAULT;

           //return;
        }
        response.setContentType("image/jpg");
        FileUtil.readFile(response, avatarPath);
    }
    /**
     * PRINT NO DEFAILT iMAGE
     * @param response
     */
    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader("Content-Type", "image/jpg");
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write("No default image");
            writer.close();
        } catch (IOException e) {
            log.error("Print no default image error", e);
        } finally {
                writer.close();
        }
    }

    /**
     * Get user information
     * @param session
     */
    @GlobalInterceptor
    @RequestMapping("getUserInfo")
    public BaseResponseVO getUserInfo(HttpSession session) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        return successResponse(loginUserDto);
    }

    /**
     * Get user space
     */
    @GlobalInterceptor
    @RequestMapping("getUserSpace")
    public BaseResponseVO getUserSpace(HttpSession session) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        UserSpaceDto userSpaceDto = systemCache.getUserSpace(loginUserDto.getUserId());
        return successResponse(userSpaceDto);
    }

    /**
     * sign out
     * @param session
     */
    @RequestMapping("logout")
    public BaseResponseVO logout(HttpSession session) {
        session.invalidate();
        return successResponse(null);
    }

    /**
     * Update user profile picture
     * @param session
     * @param avatar
     * @return
     */
    @GlobalInterceptor
    @RequestMapping("updateUserAvatar")
    public BaseResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar) {
        try {
            SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
            String targetFolder = systemConfig.getRootPath() + SystemConstants.USER_AVATAR_FOLDER;
            File targetFolderFile = new File(targetFolder);
            if (!targetFolderFile.exists()) {
                targetFolderFile.mkdirs();
            }
            File targetFile = new File(targetFolderFile.getPath() + "/" + loginUserDto.getUserId() + SystemConstants.USER_AVATAR_SUFFIX);
            avatar.transferTo(targetFile);

            // 上Upload the avatar and then the qq avatar
            //SysUser updateUser = new SysUser();
            //updateUser.setId(loginUserDto.getUserId());
            //updateUser.setQqAvatar("");
            //userService.updateByUserId(updateUser);

            loginUserDto.setAvatar(null);
            session.setAttribute(SystemConstants.SESSION_LOGIN_USER, loginUserDto);
        } catch (IOException e) {
            log.error("Failed to upload avatar", e);
        }
        return successResponse(null);
    }

    /**
     * change Password
     * @param session
     * @param password
     * @return
     */
    @GlobalInterceptor(valid = true)
    @RequestMapping("updatePassword")
    public BaseResponseVO updatePassword(HttpSession session, @Valid(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password) {
        SessionLoginUserDto loginUserDto = getUserInfoFromSession(session);
        SysUser updateUser = new SysUser();
        updateUser.setId(loginUserDto.getUserId());
        updateUser.setPassword(DigestUtils.md5Hex(password));
        userService.updateByUserId(updateUser);
        return successResponse(null);
    }

    /**
     * QQ Login
     * @param session
     * @param redirectUrl
     * @return
     * @throws Exception
     */
    @GlobalInterceptor(login = false)
    @RequestMapping("qqLogin")
    public BaseResponseVO qqLogin(HttpSession session, String redirectUrl) throws Exception {
        String state = RandomStringUtils.random(SystemConstants.LENGTH_30, true, true);
        // 登录后跳转地址
        if (StringUtils.isNotEmpty(redirectUrl)) {
            session.setAttribute(state, redirectUrl);
        }

        String url = String.format(qqLoginConfig.getAuthorizeUrl(), qqLoginConfig.getAppId(), URLEncoder.encode(qqLoginConfig.getLoginCallbackUrl(), "utf-8"), state);
        return successResponse(url);
    }

    /**
     * QQ login callback
     * @param session
     * @param code
     * @param state
     * @return
     */
    @GlobalInterceptor(login = false, valid = true)
    @RequestMapping("qqLogin/callback")
    public BaseResponseVO qqLoginCallback(HttpSession session, @Valid(required = true) String code, @Valid(required = true) String state) {
        SessionLoginUserDto loginUserDto = userService.qqLogin(code);
        session.setAttribute(SystemConstants.SESSION_LOGIN_USER, loginUserDto);

        Map<String, Object> result = new HashMap<>();
        result.put("redirectUrl", session.getAttribute(state));
        result.put("userInfo", loginUserDto);
        return successResponse(result);
    }
}
