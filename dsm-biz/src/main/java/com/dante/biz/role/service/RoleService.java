package com.dante.biz.role.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dante.biz.permission.dao.PermissionMapper;
import com.dante.model.permission.Permission;
import com.dante.biz.role.dao.RoleMapper;
import com.dante.biz.role.dao.RolePermissionMapper;
import com.dante.model.role.Role;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
@Service("roleService")
public class RoleService implements IRoleService {
	
	@Autowired
	private RoleMapper roleMapper;
	
	@Autowired
	private RolePermissionMapper rolePermissionMapper;
	
	@Autowired
	private PermissionMapper permissionMapper;

	@Override
	public Role getRoleById(Role role) throws Exception {
		Role result = roleMapper.selectByPrimaryKey(role.getRoleId());
		if(result!=null){
			List<Permission> permissionList =permissionMapper.getRolePermissionList(result);
			result.setPermissionList(permissionList);
		}
		return result;
	}

	@Override
	public List<Role> queryRoleList(Map<String, Object> params) throws Exception {
		return roleMapper.queryRoleList(params);
	}

	@Override
	public void addRole(Role role) throws Exception {
		if(role.getPermissionList()!=null&&role.getPermissionList().size()>0){
			rolePermissionMapper.insertAllByRole(role);
			rolePermissionMapper.deleteRepeatByRole(role);
		}
		roleMapper.insert(role);
	}

	@Override
	public int deleteRole(Role role) throws Exception {
		rolePermissionMapper.deleteByRole(role);
		return roleMapper.deleteByPrimaryKey(role.getRoleId());
	}

	@Override
	public int updateRole(Role role) throws Exception {
		if(role.getPermissionList()!=null&&role.getPermissionList().size()>0){
			rolePermissionMapper.insertAllByRole(role);
			rolePermissionMapper.deleteRepeatByRole(role);
		}else{
			rolePermissionMapper.deleteByRole(role);
		}
		return roleMapper.updateByPrimaryKeySelective(role);
	}

	@Override
	public PageInfo<Role> pageRoleList(Map<String, Object> params) throws Exception {
		Page<Role> result = PageHelper.startPage((Integer)params.get("pageNum"), (Integer)params.get("pageSize"));
		roleMapper.queryRoleList(params); 
	    return result.toPageInfo();
	}

}
