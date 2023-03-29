
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

package com.albedo.java.modules.sys.domain.vo;

import com.albedo.java.common.core.annotation.DictType;
import com.albedo.java.common.core.constant.DictNameConstants;
import com.albedo.java.common.core.domain.vo.TreeVo;
import com.albedo.java.common.core.util.tree.TreeNodeAware;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 字典表
 * </p>
 *
 * @author somewhere
 * @since 2019/2/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictVo extends TreeVo<DictVo> implements TreeNodeAware<DictVo> {

	private static final long serialVersionUID = 1L;

	/**
	 * 数据值
	 */
	private String val;

	/**
	 * 类型
	 */
	private String code;

	@DictType(DictNameConstants.SYS_FLAG)
	private Integer available;

}
