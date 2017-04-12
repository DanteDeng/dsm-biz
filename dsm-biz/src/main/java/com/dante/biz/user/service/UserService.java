package com.dante.biz.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dante.biz.permission.dao.PermissionMapper;
import com.dante.model.permission.Permission;
import com.dante.biz.role.dao.RoleMapper;
import com.dante.model.role.Role;
import com.dante.biz.user.dao.UserMapper;
import com.dante.biz.user.dao.UserRoleMapper;
import com.dante.model.user.User;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
@Service("userService")
public class UserService implements IUserService{
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private UserRoleMapper userRoleMapper;
	
	@Autowired
	private RoleMapper roleMapper;
	
	@Autowired
	private PermissionMapper permissionMapper;

	@Override
	public List<User> queryUserList(Map<String, Object> params) throws Exception {
		return userMapper.queryUserList(params);
	}

	@Override
	public void addUser(User user) throws Exception {
		if(user.getRoleList()!=null&&user.getRoleList().size()>0){
			userRoleMapper.insertAllByUser(user);
			userRoleMapper.deleteRepeatByUser(user);
		}
		userMapper.insert(user);
	}

	@Override
	public User getUserById(User user) throws Exception {
		User result = userMapper.selectByPrimaryKey(user.getUserId());
		if(result!=null){
			List<Role> roleList = roleMapper.queryUserRoleList(result);
			result.setRoleList(roleList);
		}
		return result;
	}

	@Override
	public int updateUser(User user) throws Exception {
		if(user.getRoleList()!=null&&user.getRoleList().size()>0){
			userRoleMapper.insertAllByUser(user);
			userRoleMapper.deleteRepeatByUser(user);
		}else{
			userRoleMapper.deleteByUser(user);
		}
		return userMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	public int deleteUser(User user) throws Exception {
		userRoleMapper.deleteByUser(user);
		return userMapper.deleteByPrimaryKey(user.getUserId());
	}

	@Override
	public PageInfo<User> pageUserList(Map<String, Object> params) throws Exception {
		Page<User> result = PageHelper.startPage((Integer)params.get("pageNum"), (Integer)params.get("pageSize"));
		userMapper.queryUserList(params); 
	    return result.toPageInfo();
	}

	@Override
	public User getUserLogin(User user) throws Exception {
		User result = userMapper.selectByLoginNameAndPwd(user);
		if(result!=null){
			List<Permission> userPermissionList = permissionMapper.getUserPermissionList(result);
			List<Permission> permissionList = null;
			for (Permission permission : userPermissionList) {	//去除非静态资源的权限
				if("1".equals(permission.getPermissionType())){
					if(permissionList==null){
						permissionList = new ArrayList<Permission>();
					}
					permissionList.add(permission);
				}
			}
			result.setPermissionList(permissionList);
		}
		return result;
	}

}
