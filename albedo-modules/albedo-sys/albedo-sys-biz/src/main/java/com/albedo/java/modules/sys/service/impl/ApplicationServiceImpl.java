
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

package com.albedo.java.modules.sys.service.impl;

import cn.hutool.core.convert.Convert;
import com.albedo.java.common.core.cache.model.CacheKey;
import com.albedo.java.common.core.cache.model.CacheKeyBuilder;
import com.albedo.java.modules.sys.cache.ApplicationClientCacheKeyBuilder;
import com.albedo.java.modules.sys.domain.ApplicationDo;
import com.albedo.java.modules.sys.domain.dto.ApplicationDto;
import com.albedo.java.modules.sys.repository.ApplicationRepository;
import com.albedo.java.modules.sys.service.ApplicationService;
import com.albedo.java.plugins.database.mybatis.conditions.Wraps;
import com.albedo.java.plugins.database.mybatis.service.impl.AbstractDataCacheServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author somewhere
 * @since 2019/2/1
 */
@Service
public class ApplicationServiceImpl extends AbstractDataCacheServiceImpl<ApplicationRepository, ApplicationDo, ApplicationDto>
	implements ApplicationService {

	@Override
	protected CacheKeyBuilder cacheKeyBuilder() {
		return null;
	}

	@Override
	public ApplicationDo getByClient(String clientId, String clientSecret) {
		LambdaQueryWrapper<ApplicationDo> wrapper = Wraps.<ApplicationDo>lambdaQueryWrapperX()
			.select(ApplicationDo::getId).eq(ApplicationDo::getClientId, clientId).eq(ApplicationDo::getClientSecret, clientSecret);
		Function<CacheKey, Object> loader = k -> super.getObj(wrapper, Convert::toLong);
		CacheKey cacheKey = new ApplicationClientCacheKeyBuilder().key(clientId, clientSecret);
		return getByKey(cacheKey, loader);
	}

}
