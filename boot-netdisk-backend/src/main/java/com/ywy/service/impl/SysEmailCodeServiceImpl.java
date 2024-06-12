package com.ywy.service.impl;

import com.ywy.cache.SystemCache;
import com.ywy.core.base.BaseServiceImpl;
import com.ywy.core.constants.SystemConstants;
import com.ywy.core.config.SystemConfig;
import com.ywy.core.exception.BusinessException;
import com.ywy.mapper.SysEmailCodeMapper;
import com.ywy.mapper.SysUserMapper;
import com.ywy.pojo.entity.SysEmailCode;
import com.ywy.pojo.entity.SysUser;
import com.ywy.pojo.param.EmailCodeParam;
import com.ywy.service.SysEmailCodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
* @author ywy
* @description 针对表【sys_email_code(邮箱验证码)】的数据库操作Service实现
* @createDate 2023-07-19 19:13:53
*/
@Slf4j
@Service
public class SysEmailCodeServiceImpl extends BaseServiceImpl<SysEmailCode, EmailCodeParam> implements SysEmailCodeService {
    @Resource
    private SysEmailCodeMapper sysEmailCodeMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private SystemConfig systemConfig;
    @Resource
    private SystemCache systemCache;

    @Override
    public void sendEmailCode(String email, Integer type) {
        // 验证码类型：0注册；1找回密码
        if (type == SystemConstants.ZERO) {
            // 如果是注册，校验邮箱是否已存在
            SysUser user = sysUserMapper.selectByEmail(email);
            if (user  != null)   throw new BusinessException("EMAIL ALREADY EXISTS");
        }
        // 生成邮箱验证码值
        String code = RandomStringUtils.random(SystemConstants.LENGTH_5, false, true);
        // 发送邮件验证码
        sendEmailCode(email, code);
        // 将之前的邮箱验证码置为无效
        sysEmailCodeMapper.disableCode(email);
        SysEmailCode emailCode = new SysEmailCode();
        emailCode.setCode(code);
        emailCode.setEmail(email);
        emailCode.setStatus(SystemConstants.ZERO);
        emailCode.setCreateTime(new Date());
        sysEmailCodeMapper.insert(emailCode);
    }

    @Override
    public void checkEmailCode(String email, String code) {
        // 查询邮箱验证码
        SysEmailCode sysEmailCode = sysEmailCodeMapper.selectByEmailAndCode(email, code);
        if (null == sysEmailCode) {
            throw new BusinessException("Email verification code is incorrect");
        }
        //The email verification code has been used or exceeded 15 minutes
        if (sysEmailCode.getStatus() == 1 || System.currentTimeMillis() - sysEmailCode.getCreateTime().getTime() > SystemConstants.LENGTH_15 * 1000 * 60) {
            throw new BusinessException("Email verification code has expired");
        }

        // 将邮箱验证码置为无效
        sysEmailCodeMapper.disableCode(email);
    }

    /**
     * 发送邮件验证码
     * @param toMail
     * @param code
     */
    private void sendEmailCode(String toMail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(systemConfig.getFromMail()); // 邮件发送人
            helper.setTo(toMail); // 邮件收件人，1个或多个
            helper.setSubject(systemCache.getSystemSettings().getRegisterEmailTitle()); // 邮件主题
            helper.setText(String.format(systemCache.getSystemSettings().getRegisterEmailContent(), code)); // 邮件内容
            helper.setSentDate(new Date()); // 邮件发送时间

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("郵箱發送失敗", e);
            throw new BusinessException("郵箱發送失敗");
        }
    }
}




