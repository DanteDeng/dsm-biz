package com.dante.biz.permission.dao;

import java.util.List;
import java.util.Map;

import com.dante.model.permission.Permission;
import com.dante.model.role.Role;
import com.dante.model.user.User;

public interface PermissionMapper {
    int deleteByPrimaryKey(String permissionId);

    int insert(Permission record);

    int insertSelective(Permission record);

    Permission selectByPrimaryKey(String permissionId);

    int updateByPrimaryKeySelective(Permission record);

    int updateByPrimaryKey(Permission record);

	List<Permission> queryPermissionList(Map<String, Object> params);

	List<Permission> getUserPermissionList(User user);

	List<Permission> getRolePermissionList(Role role);
}