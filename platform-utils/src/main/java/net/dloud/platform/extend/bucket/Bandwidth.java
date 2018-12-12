/*
 *
 * Copyright 2015-2018 Vladimir Bukhtoyarov
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package net.dloud.platform.extend.bucket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * @author QuDasheng
 * @create 2018-12-10 15:15
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bandwidth {
    /**
     * 初始化的时间戳
     */
    private long initial;

    /**
     * 每段内可使用的全部token
     */
    private long capacity;

    /**
     * 每段内已使用的token
     */
    private long consumed;

    /**
     * 每段的时间间隔，毫秒
     */
    private long interval;


    public static Bandwidth simple(long capacity, Duration period) {
        return new Bandwidth(System.currentTimeMillis(), capacity, 0L, period.toMillis());
    }
}