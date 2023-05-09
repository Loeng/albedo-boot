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

import com.albedo.java.common.core.domain.vo.PageModel;
import com.albedo.java.common.core.util.Result;
import com.albedo.java.common.log.annotation.LogOperate;
import com.albedo.java.common.log.enums.LogType;
import com.albedo.java.common.security.util.SecurityUtil;
import com.albedo.java.common.util.ExcelUtil;
import com.albedo.java.modules.sys.domain.LogOperateDo;
import com.albedo.java.modules.sys.domain.dto.LogOperateQueryDto;
import com.albedo.java.modules.sys.service.LogOperateService;
import com.albedo.java.plugins.database.mybatis.util.QueryWrapperUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 操作日志表 前端控制器
 * </p>
 *
 * @author somewhere
 * @since 2019/2/1
 */
@RestController
@AllArgsConstructor
@RequestMapping("${application.admin-path}/sys/log-operate")
@Tag(name = "操作日志")
public class LogOperateResource {

	private final LogOperateService logOperateService;

	/**
	 * 简单分页查询
	 *
	 * @param pageModel 分页对象
	 * @return
	 */
	@GetMapping
	@PreAuthorize("@pms.hasPermission('sys_logOperate_view')")
	public Result<IPage<LogOperateDo>> getPage(PageModel<LogOperateDo> pageModel, LogOperateQueryDto logOperateQueryDto) {
		return Result.buildOkData(logOperateService.page(pageModel, QueryWrapperUtil.getWrapper(pageModel, logOperateQueryDto)));
	}

	/**
	 * 删除操作日志
	 *
	 * @param ids ID
	 * @return success/false
	 */
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_logOperate_del')")
	@LogOperate(value = "操作日志删除")
	public Result<?> removeById(@RequestBody Set<Long> ids) {
		return Result.buildOkData(logOperateService.removeByIds(ids));
	}

	@LogOperate(value = "操作日志导出")
	@GetMapping(value = "/download")
	@PreAuthorize("@pms.hasPermission('sys_logOperate_export')")
	public void download(LogOperateQueryDto logOperateQueryDto, HttpServletResponse response) {
		QueryWrapper<LogOperateDo> wrapper = QueryWrapperUtil.getWrapper(logOperateQueryDto);
		ExcelUtil<LogOperateDo> util = new ExcelUtil<>(LogOperateDo.class);
		List<LogOperateDo> list = logOperateService.list(wrapper);
		util.exportExcel(list, "操作日志", response);
	}

	@GetMapping(value = "/user")
	@Operation(summary = "用户日志查询")
	public Result<IPage<LogOperateDo>> getUserLogs(PageModel<LogOperateDo> pageModel, LogOperateQueryDto criteria) {
		criteria.setLogType(Lists.newArrayList(LogType.INFO.name(), LogType.WARN.name()));
		criteria.setUsername(SecurityUtil.getUser().getUsername());
		pageModel.addOrder(OrderItem.desc(LogOperateDo.F_SQL_CREATED_DATE));
		QueryWrapper<LogOperateDo> wrapper = QueryWrapperUtil.getWrapper(
			pageModel, criteria);

		return Result.buildOkData(logOperateService.page(pageModel, wrapper));
	}

}
