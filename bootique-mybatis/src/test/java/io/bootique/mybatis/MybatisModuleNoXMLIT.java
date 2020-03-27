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
import io.bootique.mybatis.testmappers1.T1Mapper;
import io.bootique.mybatis.testmappers2.T2Mapper;
import io.bootique.mybatis.testpojos.TO1;
import io.bootique.mybatis.testpojos.TO2;
import io.bootique.test.junit.BQTestFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class MybatisModuleNoXMLIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    private static BQRuntime runtime;
    private static DatabaseChannel channel;
    private static SqlSessionManager sessionManager;

    @BeforeClass
    public static void setupDB() {
        runtime = TEST_FACTORY
                .app("--config=classpath:io/bootique/mybatis/MybatisModuleNoXMLIT.yml")
                .autoLoadModules()
                .module(b -> MybatisModule.extend(b)
                        .addMapperPackage(T1Mapper.class.getPackage())
                        .addMapper(T2Mapper.class))
                .createRuntime();

        channel = DatabaseChannel.get(runtime);
        sessionManager = runtime.getInstance(SqlSessionManager.class);
    }

    private Table createT1() {
        channel.execStatement().exec("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        return channel.newTable("t1").columnNames("c1", "c2", "c3").initColumnTypesFromDBMetadata().build();
    }

    private Table createT2() {
        channel.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" VARCHAR(10))");
        return channel.newTable("t2").columnNames("c1", "c2").initColumnTypesFromDBMetadata().build();
    }

    @Test
    public void testSqlSessionManager_PackageMappers() {

        createT1().insertColumns("c1", "c2", "c3")
                .values(5, "a", "c")
                .exec();

        try (SqlSession session = sessionManager.openSession()) {

            T1Mapper t1Mapper = session.getMapper(T1Mapper.class);
            Optional<TO1> miss = t1Mapper.find(3L);
            assertFalse(miss.isPresent());

            Optional<TO1> hit = t1Mapper.find(5L);
            assertTrue(hit.isPresent());
            assertEquals(5, hit.get().getC1());
            assertEquals("a", hit.get().getC2());
            assertEquals("c", hit.get().getC3());
            assertEquals("c", hit.get().getC3_x());
        }
    }

    @Test
    public void testSqlSessionManager_ClassMappers() {

        createT2().insertColumns("c1", "c2")
                .values(6, "x")
                .exec();

        try (SqlSession session = sessionManager.openSession()) {

            T2Mapper t2Mapper = session.getMapper(T2Mapper.class);
            Optional<TO2> miss = t2Mapper.find(3L);
            assertFalse(miss.isPresent());

            Optional<TO2> hit = t2Mapper.find(6L);
            assertTrue(hit.isPresent());
            assertEquals(6, hit.get().getC1());
            assertEquals("x", hit.get().getC2());
        }
    }
}
