package org.opengauss.admin.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opengauss.admin.common.core.domain.BaseEntity;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * SysUser Model
 *
 * @author xielibo
 */
@Data
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    public SysUser() {

    }

    public SysUser(Integer userId) {
        this.userId = userId;
    }

    /**
     * userId
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * userName
     */
    private String userName;

    /**
     * nickName
     */
    private String nickName;

    /**
     * email
     */
    private String email;

    /**
     * phonenumber
     */
    private String phonenumber;

    /**
     * sex
     */
    private String sex;

    /**
     * avatar
     */
    private String avatar;

    /**
     * password
     */
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String password;

    /**
     * salt
     */
    @TableField(exist = false)
    private String salt;

    /**
     * status(0:normal 1:disabled)
     */
    private String status;

    /**
     * delFlag(0 No 1 Yes)
     */
    @TableLogic
    private String delFlag;

    /**
     * updatePwd (0 No 1 Yes)
     */
    private String updatePwd;

    /**
     * loginIp
     */
    @TableField(fill = FieldFill.INSERT)
    private String loginIp;

    /**
     * roles
     */
    @TableField(exist = false)
    private List<SysRole> roles;

    /**
     * roleIds
     */
    @TableField(exist = false)
    private Integer roleIds;

    @TableField(exist = false)
    private String roleName;


    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Integer userId) {
        return userId != null && userId.equals(1);
    }


    @Size(min = 0, max = 30, message = "User nickname length cannot exceed 30 characters")
    public String getNickName() {
        return nickName;
    }


    @NotBlank(message = "User account cannot be empty")
    @Size(min = 0, max = 30, message = "User account length cannot exceed 30 characters")
    public String getUserName() {
        return userName;
    }


    @Email(message = "E-mail format is incorrect")
    @Size(min = 0, max = 50, message = "E-mail length cannot exceed 50 characters")
    public String getEmail() {
        return email;
    }


    @Size(min = 0, max = 11, message = "Mobile phone number length cannot exceed 11 characters")
    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    @JsonIgnore
    @JsonProperty
    public String getPassword() {
        return password;
    }
}
