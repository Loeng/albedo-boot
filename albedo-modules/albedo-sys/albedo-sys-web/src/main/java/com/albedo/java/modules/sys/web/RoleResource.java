
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

package com.albedo.java.modules.sys.web;

import com.albedo.java.common.core.constant.CommonConstants;
import com.albedo.java.common.core.domain.vo.PageModel;
import com.albedo.java.common.core.exception.BizException;
import com.albedo.java.common.core.util.ArgumentAssert;
import com.albedo.java.common.core.util.CollUtil;
import com.albedo.java.common.core.util.Result;
import com.albedo.java.common.core.util.StringUtil;
import com.albedo.java.common.log.annotation.LogOperate;
import com.albedo.java.common.security.util.SecurityUtil;
import com.albedo.java.common.web.resource.BaseResource;
import com.albedo.java.modules.sys.domain.RoleDo;
import com.albedo.java.modules.sys.domain.UserDo;
import com.albedo.java.modules.sys.domain.dto.RoleDto;
import com.albedo.java.modules.sys.domain.dto.RoleMenuDto;
import com.albedo.java.modules.sys.domain.dto.RoleQueryDto;
import com.albedo.java.modules.sys.domain.vo.RoleComboVo;
import com.albedo.java.modules.sys.service.RoleMenuService;
import com.albedo.java.modules.sys.service.RoleService;
import com.albedo.java.modules.sys.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author somewhere
 * @date 2019/2/1
 */
@RestController
@RequestMapping("${application.admin-path}/sys/role")
@AllArgsConstructor
@Tag(name = "角色管理")
public class RoleResource extends BaseResource {

	private final RoleService roleService;

	private final RoleMenuService roleMenuService;

	private final UserService userService;

	/**
	 * @param id
	 * @return
	 */
	@GetMapping(CommonConstants.URL_ID_REGEX)
	public Result<RoleDto> get(@PathVariable Long id) {
		log.debug("REST request to get Entity : {}", id);
		return Result.buildOkData(roleService.getOneDto(id));
	}

	/**
	 * 添加角色
	 *
	 * @param roleDto 角色信息
	 * @return success、false
	 */
	@LogOperate(value = "角色管理编辑")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_role_edit')")
	public Result<?> save(@Valid @RequestBody RoleDto roleDto) {
		checkLevel(roleDto.getLevel());
		roleService.saveOrUpdate(roleDto);
		return Result.buildOk("操作成功");
	}

	/**
	 * 获取用户级别
	 *
	 * @return
	 */
	@Operation(summary = "获取用户级别")
	@GetMapping(value = "/level")
	public Result<Integer> findLevel() {
		return Result.buildOkData(roleService.findLevelByUserId(SecurityUtil.getUser().getId()));
	}

	/**
	 * 获取角色列表
	 *
	 * @return 角色列表
	 */
	@GetMapping("/all")
	public Result<List<RoleComboVo>> all() {
		return Result.buildOkData(
			roleService.list(Wrappers.<RoleDo>lambdaQuery().eq(RoleDo::getAvailable, CommonConstants.STR_YES)).stream()
				.map(RoleComboVo::new).collect(Collectors.toList()));
	}

	/**
	 * 分页查询角色信息
	 *
	 * @param pageModel 分页对象
	 * @return 分页对象
	 */
	@GetMapping
	@LogOperate(value = "角色管理查看")
	public Result<IPage<RoleDo>> findPage(PageModel<RoleDo> pageModel, RoleQueryDto roleQueryDto) {
		return Result.buildOkData(roleService.findPage(pageModel, roleQueryDto));
	}

	/**
	 * 更新角色菜单
	 *
	 * @param roleMenuDto 角色菜单
	 * @return success、false
	 */
	@PutMapping("/menu")
	@LogOperate(value = "角色管理编辑")
	@PreAuthorize("@pms.hasPermission('sys_role_edit')")
	public Result<?> saveRoleMenus(@Valid @RequestBody RoleMenuDto roleMenuDto) {
		RoleDo roleDo = roleService.getById(roleMenuDto.getRoleId());
		checkLevel(roleDo.getLevel());
		return roleMenuService.saveRoleMenus(roleMenuDto);
	}

	/**
	 * 删除角色
	 *
	 * @param ids
	 * @return
	 */
	@LogOperate(value = "角色管理删除")
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_role_del')")
	public Result<?> removeByIds(@RequestBody Set<Long> ids) {
		roleService.listByIds(ids).forEach(item -> {
			checkLevel(item.getLevel());
			checkRole(item.getId(), item.getName());
		});
		roleService.removeRoleByIds(ids);
		return Result.buildOk("操作成功");
	}

	/**
	 * @param ids
	 * @return
	 */
	@PutMapping
	@LogOperate(value = "角色管理锁定/解锁")
	@PreAuthorize("@pms.hasPermission('sys_role_lock')")
	public Result<?> lockOrUnLock(@RequestBody Set<Long> ids) {
		roleService.listByIds(ids).forEach(item -> {
			checkLevel(item.getLevel());
			checkRole(item.getId(), item.getName());
		});
		roleService.lockOrUnLock(ids);
		return Result.buildOk("操作成功");
	}

	/**
	 * 获取用户的角色级别
	 */
	private void checkLevel(Integer level) {
		int min = roleService.findLevelByUserId(SecurityUtil.getUser().getId());
		if (level != null) {
			if (level < min) {
				throw new BizException("权限不足，你的角色级别：" + min + "，低于操作的角色级别：" + level);
			}
		}
	}

	/**
	 * 检查角色是否有用户信息
	 *
	 * @return
	 */
	private void checkRole(Long roleId, String roleName) {
		List<UserDo> userDoList = userService.findListByRoleId(roleId);
		ArgumentAssert.empty(userDoList, () -> new BizException("操作失败！用户："
			+ CollUtil.convertToString(userDoList, UserDo.F_USERNAME, StringUtil.COMMA) + "所属要操作的角色：" + roleName));
	}

}
