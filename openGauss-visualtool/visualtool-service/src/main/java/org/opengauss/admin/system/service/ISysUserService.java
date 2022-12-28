package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service Interface
 *
 * @author xielibo
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * Query user page list
     *
     * @param user user param
     * @return page list
     */
    public IPage<SysUser> selectUserList(SysUser user, IPage<SysUser> page);

    /**
     * Query by username
     *
     * @param userName username
     * @return SysUser
     */
    public SysUser selectUserByUserName(String userName);

    /**
     * get By Id
     *
     * @param userId user ID
     * @return SysUser
     */
    public SysUser selectUserById(Integer userId);

    /**
     * Query the user's role
     *
     * @param userName username
     */
    public String selectUserRoleGroup(String userName);

    /**
     * Check if username is unique
     *
     * @param userName username
     */
    public String checkUserNameUnique(String userName);

    /**
     * Check if phone is unique
     *
     * @param user user
     * @return
     */
    public String checkPhoneUnique(SysUser user);

    /**
     * Check if email is unique
     *
     * @param user user
     * @return
     */
    public String checkEmailUnique(SysUser user);

    /**
     * Check if the user allows the operation
     *
     * @param user user
     */
    public void checkUserAllowed(SysUser user);

    /**
     * save user
     *
     */
    public int insertUser(SysUser user);

    /**
     * update user
     *
     */
    public int updateUser(SysUser user);


    /**
     * update status
     *
     */
    public int updateUserStatus(SysUser user);

    /**
     * updat information
     *
     */
    public int updateUserProfile(SysUser user);

    /**
     * update user avatar
     *
     */
    public boolean updateUserAvatar(String userName, String avatar);

    /**
     * reset password
     *
     */
    public int resetPwd(SysUser user);

    /**
     * reset password
     *
     */
    public int resetUserPwd(String userName, String password);

    public int deleteUserById(Integer userId);

    public int deleteUserByIds(Integer[] userIds);
}
