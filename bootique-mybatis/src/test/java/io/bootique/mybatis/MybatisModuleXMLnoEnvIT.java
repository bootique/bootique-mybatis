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
import io.bootique.jdbc.test.DatabaseChannel;
import io.bootique.jdbc.test.Table;
import io.bootique.mybatis.testmappersxml1.T3Mapper;
import io.bootique.mybatis.testpojos.TO3;
import io.bootique.test.junit.BQTestFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class MybatisModuleXMLnoEnvIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    private static BQRuntime runtime;
    private static DatabaseChannel channel;
    private static SqlSessionManager sessionManager;

    @BeforeClass
    public static void setupDB() {
        runtime = TEST_FACTORY
                .app("--config=classpath:io/bootique/mybatis/MybatisModuleXMLnoEnvIT.yml")
                .autoLoadModules()
                .createRuntime();

        channel = DatabaseChannel.get(runtime);
        sessionManager = runtime.getInstance(SqlSessionManager.class);
    }

    private Table createT3() {
        channel.execStatement().exec("CREATE TABLE \"t3\" (\"c1\" INT, \"c2\" VARCHAR(10))");
        return channel.newTable("t3").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testSqlSessionManager() {

        createT3().insertColumns("c1", "c2")
                .values(6, "x")
                .exec();

        try (SqlSession session = sessionManager.openSession()) {

            T3Mapper mapper = session.getMapper(T3Mapper.class);
            Optional<TO3> miss = mapper.find(3L);
            assertFalse(miss.isPresent());

            Optional<TO3> hit = mapper.find(6L);
            assertTrue(hit.isPresent());
            assertEquals(6, hit.get().getC1());
            assertEquals("x", hit.get().getC2());
        }
    }
}
