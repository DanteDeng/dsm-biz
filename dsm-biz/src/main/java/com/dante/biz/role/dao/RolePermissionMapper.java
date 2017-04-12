package com.dante.biz.role.dao;

import com.dante.model.role.Role;
import com.dante.model.role.RolePermission;

public interface RolePermissionMapper {
    int insert(RolePermission record);

    int insertSelective(RolePermission record);

	void insertAllByRole(Role role);

	int deleteRepeatByRole(Role role);

	int deleteByRole(Role role);
}