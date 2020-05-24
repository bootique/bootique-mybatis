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
import io.bootique.jdbc.test.DbTester;
import io.bootique.mybatis.testmappers1.T1Mapper;
import io.bootique.mybatis.testmappers1.T5Mapper;
import io.bootique.mybatis.testmappers2.T2Mapper;
import io.bootique.mybatis.testpojos.TO1;
import io.bootique.mybatis.testpojos.TO2;
import io.bootique.mybatis.testpojos.TO5;
import io.bootique.mybatis.testtypehandlers1.V1Handler;
import io.bootique.mybatis.testtypehandlers2.V3Handler;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class MybatisModuleNoXMLIT {

    @RegisterExtension
    static final DbTester db = DbTester.derbyDb();

    @BQApp(skipRun = true)
    private static BQRuntime app = Bootique
            .app()
            .autoLoadModules()
            .module(db.setOrReplaceDataSource("db"))
            .module(b -> MybatisModule.extend(b)
                    .addMapperPackage(T1Mapper.class.getPackage())
                    .addMapper(T2Mapper.class)
                    .addTypeHandlerPackage(V3Handler.class)
                    .addTypeHandler(V1Handler.class))
            .createRuntime();

    static SqlSessionManager getSessionManager() {
        return app.getInstance(SqlSessionManager.class);
    }

    @BeforeAll
    static void createSchema() {
        db.execStatement().exec("CREATE TABLE \"t1\" (\"c1\" INT, \"c2\" VARCHAR(10), \"c3\" VARCHAR(10))");
        db.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" VARCHAR(10))");
        db.execStatement().exec("CREATE TABLE \"t5\" (\"c1\" INT, \"c2\" INT, \"c3\" INT, \"c4\" INT)");
    }

    @Test
    public void testSqlSessionManager_PackageMappers() {

        db.getTable("t1").insertColumns("c1", "c2", "c3")
                .values(5, "a", "c")
                .exec();

        try (SqlSession session = getSessionManager().openSession()) {

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

        db.getTable("t2").insertColumns("c1", "c2")
                .values(6, "x")
                .exec();

        try (SqlSession session = getSessionManager().openSession()) {

            T2Mapper t2Mapper = session.getMapper(T2Mapper.class);
            Optional<TO2> miss = t2Mapper.find(3L);
            assertFalse(miss.isPresent());

            Optional<TO2> hit = t2Mapper.find(6L);
            assertTrue(hit.isPresent());
            assertEquals(6, hit.get().getC1());
            assertEquals("x", hit.get().getC2());
        }
    }

    @Test
    public void testSqlSessionManager_TypeHandlers() {

        db.getTable("t5").insertColumns("c1", "c2", "c3", "c4")
                .values(6, 15, 24, 66)
                .exec();

        try (SqlSession session = getSessionManager().openSession()) {

            T5Mapper mapper = session.getMapper(T5Mapper.class);

            Optional<TO5> hit = mapper.find(6L);
            assertTrue(hit.isPresent());
            assertEquals(6, hit.get().getC1());
            assertEquals(15, hit.get().getC2().getV());
            assertEquals(24, hit.get().getC3().getV());
            assertEquals(66, hit.get().getC4().getV());
        }
    }
}
