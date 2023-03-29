
/*
 *  Copyright (c) 2019-2023  <a href="https://github.com/somowhere/albedo">Albedo</a>, somewhere (somewhere0813@gmail.com).
 *  <p>
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 * https://www.gnu.org/licenses/lgpl.html
 *  <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.albedo.java.common.security.service;

import cn.hutool.core.util.ArrayUtil;
import com.albedo.java.common.core.constant.SecurityConstants;
import com.albedo.java.common.core.util.ArgumentAssert;
import com.albedo.java.common.core.util.CollUtil;
import com.albedo.java.common.security.util.AuthUtil;
import com.albedo.java.modules.sys.domain.RoleDo;
import com.albedo.java.modules.sys.domain.enums.DataScopeType;
import com.albedo.java.modules.sys.domain.vo.UserInfo;
import com.albedo.java.modules.sys.domain.vo.UserVo;
import com.albedo.java.modules.sys.feign.RemoteDeptService;
import com.albedo.java.modules.sys.feign.RemoteRoleService;
import com.albedo.java.modules.sys.feign.RemoteUserService;
import com.albedo.java.plugins.database.mybatis.datascope.DataScope;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户详细信息
 *
 * @author somewhere
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final RemoteUserService userService;

	private final RemoteRoleService roleService;

	private final RemoteDeptService deptService;

	/**
	 * 用户密码登录
	 *
	 * @param username 用户名
	 * @return
	 */
	@Override
	@SneakyThrows
	public UserDetails loadUserByUsername(String username) {
		UserVo userVo = userService.findVoByUsername(username);
		ArgumentAssert.notNull(userVo, () -> new UsernameNotFoundException("用户不存在"));
		ArgumentAssert.isTrue(userVo.isAvailable(), "用户【" + username + "】已被锁定，无法登录");
		UserDetails userDetails = getUserDetails(userService.getInfo(userVo));
		return userDetails;
	}

	/**
	 * 构建userdetails
	 *
	 * @param userInfo 用户信息
	 * @return
	 */
	private UserDetails getUserDetails(UserInfo userInfo) {
		ArgumentAssert.notNull(userInfo, () -> new UsernameNotFoundException("用户不存在"));

		Set<String> dbAuthsSet = new HashSet<>();
		if (ArrayUtil.isNotEmpty(userInfo.getRoles())) {
			// 获取角色
			Arrays.stream(userInfo.getRoles()).forEach(role -> dbAuthsSet.add(SecurityConstants.ROLE + role));
			// 获取资源
			dbAuthsSet.addAll(Arrays.asList(userInfo.getPermissions()));

		}
		List<GrantedAuthority> authorities = AuthUtil.createAuthorityList(dbAuthsSet.toArray(new String[0]));
		UserVo userVo = userInfo.getUser();
		DataScope dataScope = new DataScope();
		if (CollUtil.isNotEmpty(userVo.getRoleList())) {
			for (RoleDo roleDo : userVo.getRoleList()) {
				if (DataScopeType.ALL.eq(roleDo.getDataScope())) {
					dataScope.setAll(true);
					break;
				} else if (DataScopeType.THIS_LEVEL_CHILDREN.eq(roleDo.getDataScope())) {
					dataScope.getDeptIds().addAll(deptService.findDescendantIdList(userVo.getDeptId()));
				} else if (DataScopeType.THIS_LEVEL.eq(roleDo.getDataScope())) {
					dataScope.getDeptIds().add(userVo.getDeptId());
				} else if (DataScopeType.SELF.eq(roleDo.getDataScope())) {
					dataScope.setSelf(true);
					dataScope.setUserId(userVo.getId());
				} else if (DataScopeType.CUSTOMIZE.eq(roleDo.getDataScope())) {
					dataScope.getDeptIds().addAll(roleService.findDeptIdsByRoleId(roleDo.getId()));
				}
			}
		}
		// 构造security用户
		return new UserDetail(userVo.getId(), userVo.getDeptId(), userVo.getDeptName(), userVo.getUsername(),
			SecurityConstants.BCRYPT + userVo.getPassword(), userVo.isAvailable(), true, true, true, authorities,
			dataScope);
	}

}
