/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.mybatis.testmappers1;

import io.bootique.mybatis.testpojos.TO5;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface T5Mapper {

    @Select("SELECT \"c1\", \"c2\", \"c3\", \"c4\" FROM \"t5\" WHERE \"c1\" = #{c1}")
    @Results(value = {
            @Result(property = "c1", column = "c1"),
            @Result(property = "c2", column = "c2"),
            @Result(property = "c3", column = "c3"),
            @Result(property = "c4", column = "c4")
    })
    Optional<TO5> find(Long c1);
}
