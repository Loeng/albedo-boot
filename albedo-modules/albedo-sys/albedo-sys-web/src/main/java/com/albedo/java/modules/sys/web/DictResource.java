
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
import com.albedo.java.common.core.domain.vo.SelectVo;
import com.albedo.java.common.core.domain.vo.TreeNode;
import com.albedo.java.common.core.util.Result;
import com.albedo.java.common.log.annotation.LogOperate;
import com.albedo.java.common.web.resource.BaseResource;
import com.albedo.java.modules.sys.domain.DictDo;
import com.albedo.java.modules.sys.domain.dto.DictDto;
import com.albedo.java.modules.sys.domain.dto.DictQueryDto;
import com.albedo.java.modules.sys.domain.vo.DictVo;
import com.albedo.java.modules.sys.service.DictService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 字典表 前端控制器
 * </p>
 *
 * @author somewhere
 * @since 2019/2/1
 */
@RestController
@RequestMapping("${application.admin-path}/sys/dict")
@AllArgsConstructor
@Tag(name = "字典管理")
public class DictResource extends BaseResource {

	private final DictService dictService;

	/**
	 * 返回树形菜单集合
	 *
	 * @return 树形菜单
	 */
	@GetMapping(value = "/tree")
	public Result<List<TreeNode<?>>> tree(DictQueryDto dictQueryDto) {
		return Result.buildOkData(dictService.findTreeNode(dictQueryDto));
	}

	/**
	 * @param id
	 * @return
	 */
	@PreAuthorize("@pms.hasPermission('sys_dict_view')")
	@GetMapping(CommonConstants.URL_ID_REGEX)
	public Result<DictDto> get(@PathVariable String id) {
		log.debug("REST request to get Entity : {}", id);
		return Result.buildOkData(dictService.getOneDto(id));
	}

	/**
	 * 查询字典信息
	 *
	 * @param dictQueryDto 查询对象
	 * @return 分页对象
	 */
	@GetMapping
	@PreAuthorize("@pms.hasPermission('sys_dict_view')")
	@LogOperate(value = "字典管理查看")
	public Result<IPage<DictVo>> findTreeList(DictQueryDto dictQueryDto) {
		IPage<DictVo> treeList = dictService.findTreeList(dictQueryDto);
		return Result.buildOkData(treeList);
	}

	/**
	 * 通过字典类型查找字典
	 *
	 * @param codes
	 * @return
	 */
	@Operation(summary = "获取字典数据", description = "codes 不传获取所有的业务字典，多个用','隔开")
	@GetMapping(value = "/codes")
	public Result<Map<String, List<SelectVo>>> getByCodes(String codes) {
		Map<String, List<SelectVo>> map = dictService.findCodes(codes);
		return Result.buildOkData(map);
	}

	/**
	 * 添加字典
	 *
	 * @param dictDto 字典信息
	 * @return success、false
	 */
	@PostMapping
	@PreAuthorize("@pms.hasPermission('sys_dict_edit')")
	@LogOperate(value = "字典管理编辑")
	public Result<?> save(@Valid @RequestBody DictDto dictDto) {
		dictService.saveOrUpdate(dictDto);
		return Result.buildOk("操作成功");
	}

	/**
	 * 删除字典，并且清除字典缓存
	 *
	 * @param ids ID
	 * @return R
	 */
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_dict_del')")
	@LogOperate(value = "字典管理删除")
	public Result<Boolean> removeByIds(@RequestBody Set<Long> ids) {
		return Result.buildOkData(dictService.removeByIds(ids));
	}

	/**
	 * @param ids
	 * @return
	 */
	@PutMapping
	@LogOperate(value = "字典管理锁定/解锁")
	@PreAuthorize("@pms.hasPermission('sys_dept_lock')")
	public Result<?> lockOrUnLock(@RequestBody Set<Long> ids) {
		dictService.lockOrUnLock(ids);
		return Result.buildOk("操作成功");
	}

	/**
	 * 所有类型字典
	 *
	 * @return 所有类型字典
	 */

	@GetMapping("/all")
	public Result<List<DictDo>> findAllList() {
		List<DictDo> list = dictService.list();
		return Result.buildOkData(list);
	}

}
