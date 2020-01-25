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
import io.bootique.jdbc.test.TestDataManager;
import io.bootique.mybatis.testmappers1.T1Mapper;
import io.bootique.mybatis.testmappers2.T2Mapper;
import io.bootique.mybatis.testpojos1.TO1;
import io.bootique.mybatis.testpojos1.TO2;
import io.bootique.test.junit.BQTestFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class MybatisModuleIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    private static Table T1;
    private static Table T2;
    private static BQRuntime RUNTIME;

    @Rule
    public TestDataManager dataManager = new TestDataManager(true, T1);

    @BeforeClass
    public static void setupDB() {
        RUNTIME = TEST_FACTORY
                .app("--config=classpath:io/bootique/mybatis/MybatisModuleIT.yml")
                .autoLoadModules()
                .module(b -> MybatisModule.extend(b)
                        .addMapperPackage(T1Mapper.class.getPackage())
                        .addMapper(T2Mapper.class))
                .createRuntime();

        DatabaseChannel channel = DatabaseChannel.get(RUNTIME);
        channel.execStatement().exec("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        channel.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" VARCHAR(10))");

        T1 = channel.newTable("t1").columnNames("c1", "c2", "c3").initColumnTypesFromDBMetadata().build();
        T2 = channel.newTable("t2").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testSqlSessionManager_PackageMappers() {

        T1.insertColumns("c1", "c2", "c3")
                .values(5, "a", "c")
                .exec();

        SqlSessionManager sessionManager = RUNTIME.getInstance(SqlSessionManager.class);
        try (SqlSession session = sessionManager.openSession()) {

            T1Mapper t1Mapper = session.getMapper(T1Mapper.class);
            Optional<TO1> miss = t1Mapper.getById(3L);
            assertFalse(miss.isPresent());

            Optional<TO1> hit = t1Mapper.getById(5L);
            assertTrue(hit.isPresent());
            assertEquals(5, hit.get().getC1());
            assertEquals("a", hit.get().getC2());
            assertEquals("c", hit.get().getC3());
            assertEquals("c", hit.get().getC3_x());
        }
    }

    @Test
    public void testSqlSessionManager_ClassMappers() {

        T2.insertColumns("c1", "c2")
                .values(6, "x")
                .exec();

        SqlSessionManager sessionManager = RUNTIME.getInstance(SqlSessionManager.class);
        try (SqlSession session = sessionManager.openSession()) {

            T2Mapper t2Mapper = session.getMapper(T2Mapper.class);
            Optional<TO2> miss = t2Mapper.getById(3L);
            assertFalse(miss.isPresent());

            Optional<TO2> hit = t2Mapper.getById(6L);
            assertTrue(hit.isPresent());
            assertEquals(6, hit.get().getC1());
            assertEquals("x", hit.get().getC2());
        }
    }
}
