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
package io.bootique.mybatis;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.mybatis.testmappers3.T7Mapper;
import io.bootique.mybatis.testpojos.TO7;
import io.bootique.test.junit5.BQApp;
import io.bootique.test.junit5.BQTest;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class MybatisModuleXMLMapperNoXMLConfigIT {

    @BQApp(skipRun = true)
    static final BQRuntime runtime = Bootique
            .app("--config=classpath:io/bootique/mybatis/MybatisModuleXMLMapperNoXMLConfigIT.yml")
            .autoLoadModules()
            .module(b -> MybatisModule.extend(b).addMapper(T7Mapper.class))
            .createRuntime();

    static final DatabaseChannel channel = DatabaseChannel.get(runtime);
    static final SqlSessionManager sessionManager = runtime.getInstance(SqlSessionManager.class);

    private Table createT7() {
        channel.execStatement().exec("CREATE TABLE \"t7\" (\"c1\" INT)");
        return channel.newTable("t7").columnNames("c1").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testSqlSessionManager_XMLMapper() {

        createT7().insertColumns("c1")
                .values(77)
                .exec();

        try (SqlSession session = sessionManager.openSession()) {

            T7Mapper mapper = session.getMapper(T7Mapper.class);
            Optional<TO7> miss = mapper.find(3L);
            assertFalse(miss.isPresent());

            Optional<TO7> hit = mapper.find(77L);
            assertTrue(hit.isPresent());
            assertEquals(77, hit.get().getC1());
        }
    }
}
