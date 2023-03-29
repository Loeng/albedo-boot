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

package com.albedo.java.modules.quartz.config;

import cn.hutool.json.JSONUtil;
import com.albedo.java.common.core.annotation.BaseInit;
import com.albedo.java.common.core.context.ContextUtil;
import com.albedo.java.common.core.domain.vo.ScheduleVo;
import com.albedo.java.common.core.exception.TaskException;
import com.albedo.java.common.core.util.ArgumentAssert;
import com.albedo.java.modules.quartz.domain.JobDo;
import com.albedo.java.modules.quartz.repository.JobRepository;
import com.albedo.java.modules.quartz.util.ScheduleUtils;
import com.albedo.java.modules.tenant.domain.Tenant;
import com.albedo.java.modules.tenant.repository.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * @author somewhere
 * @description
 * @date 2020/5/31 17:11
 */
@Slf4j
@AllArgsConstructor
@BaseInit(method = "refresh")
public class ScheduleReceiver implements MessageListener {

	public final static String DEFAULT_QUARTZ_REGISTRY_KEY = "albedo-quartz-message-lock";

	private final Scheduler scheduler;

	private final JobRepository jobRepository;

	private final TenantRepository tenantRepository;

	private final RedissonClient redissonClient;

	private final RedisSerializer serializer;

	/**
	 * 项目启动时，初始化定时器 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
	 */
	public void refresh() throws TaskException, SchedulerException {
		scheduler.clear();

		List<Tenant> tenants = tenantRepository.selectList(null);
		for (Tenant tenant : tenants) {
			ContextUtil.setTenant(tenant.getCode());
			List<JobDo> jobList = jobRepository.selectList(null);
			for (JobDo job : jobList) {
				updateSchedulerJob(job, job.getGroup());
			}
		}
	}

	/**
	 * 更新任务
	 *
	 * @param job         任务对象
	 * @param jobOldGroup 任务组名
	 */
	public void updateSchedulerJob(JobDo job, String jobOldGroup) throws SchedulerException, TaskException {
		Long jobId = job.getId();

		// 判断是否存在
		JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobOldGroup);
		if (scheduler.checkExists(jobKey)) {
			// 防止创建时存在数据问题 先移除，然后在执行创建操作
			scheduler.deleteJob(jobKey);
		}
		ScheduleUtils.createScheduleJob(scheduler, job);
	}

	/**
	 * 收到通道的消息之后执行的方法
	 *
	 * @param message
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		if (log.isDebugEnabled()) {
			log.debug("receiveMessage===>" + message);
		}
		Lock lock = redissonClient.getLock(DEFAULT_QUARTZ_REGISTRY_KEY);
		lock.lock();
		try {
			ScheduleVo scheduleVo = (ScheduleVo) serializer.deserialize(message.getBody());
			if (log.isDebugEnabled()) {
				log.debug("receiveMessage scheduleVo===>" + scheduleVo);
			}
			ArgumentAssert.notNull(scheduleVo, "scheduleVo cannot be null");
			ArgumentAssert.notNull(scheduleVo.getMessageType(), "scheduleVo messageType cannot be null");
			ArgumentAssert.notEmpty(scheduleVo.getTenantCode(), "scheduleVo tenantCode cannot be empty");
			Long jobId = scheduleVo.getJobId();
			String jobGroup = scheduleVo.getJobGroup();
			ContextUtil.setTenant(scheduleVo.getTenantCode());
			switch (scheduleVo.getMessageType()) {
				case ADD:
					ScheduleUtils.createScheduleJob(scheduler, JSONUtil.toBean(scheduleVo.getData(), JobDo.class));
					break;
				case UPDATE:
					updateSchedulerJob(JSONUtil.toBean(scheduleVo.getData(), JobDo.class), jobGroup);
					break;
				case PAUSE:
					scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
					break;
				case RESUME:
					scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
					break;
				case DELETE:
					scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, jobGroup));
					break;
				case RUN:
					scheduler.triggerJob(ScheduleUtils.getJobKey(jobId, jobGroup));
					break;
				default:
					log.warn("unknown message type :" + message);
					break;
			}
		} catch (Exception e) {
			log.warn("error message type {}", e);
		} finally {
			lock.unlock();
		}
	}

}
