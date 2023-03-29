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

package com.albedo.java.modules.monitor.domain;

/**
 * redis key数量 / 占用内存 /redis信息
 *
 * @author somewhere
 * @date 2021-03-08
 */
public enum ReportSearchType {

	/**
	 * key数量
	 */
	KEY,
	/**
	 * 占用内存
	 */
	RAM,
	/**
	 * redis信息
	 */
	INFO

}
