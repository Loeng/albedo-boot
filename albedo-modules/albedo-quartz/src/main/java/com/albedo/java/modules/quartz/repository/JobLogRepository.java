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

package com.albedo.java.modules.quartz.repository;

import com.albedo.java.modules.quartz.domain.JobLogDo;
import com.albedo.java.modules.quartz.domain.vo.JobLogExcelVo;
import com.albedo.java.plugins.database.mybatis.repository.BaseRepository;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务调度日志Repository 任务调度日志
 *
 * @author admin
 * @version 2019-08-14 11:25:03
 */
@Mapper
public interface JobLogRepository extends BaseRepository<JobLogDo> {

	/**
	 * 清空日志
	 */
	void cleanJobLog();

	/**
	 * 获取导出集合
	 *
	 * @param wrapper
	 * @return
	 */
	List<JobLogExcelVo> findExcelVo(@Param(Constants.WRAPPER) Wrapper wrapper);

}
