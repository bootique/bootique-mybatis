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
import io.bootique.jdbc.junit5.derby.DerbyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import io.bootique.mybatis.testmappersxml1.T3Mapper;
import io.bootique.mybatis.testmappersxml1.T6Mapper;
import io.bootique.mybatis.testpojos.TO3;
import io.bootique.mybatis.testpojos.TO6;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class MybatisModuleXMLnoEnvIT {

    @BQTestTool
    static final DerbyTester db = DerbyTester.db();

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app("--config=classpath:io/bootique/mybatis/MybatisModuleXMLnoEnvIT.yml")
            .autoLoadModules()
            .module(db.moduleWithTestDataSource("db"))
            .createRuntime();

    static SqlSessionManager getSessionManager() {
        return app.getInstance(SqlSessionManager.class);
    }

    @BeforeAll
    static void createSchema() {
        db.execStatement().exec("CREATE TABLE \"t3\" (\"c1\" INT, \"c2\" VARCHAR(10))");
        db.execStatement().exec("CREATE TABLE \"t6\" (\"c1\" INT, \"c2\" INT)");
    }

    @Test
    public void sqlSessionManager() {

        db.getTable("t3").insertColumns("c1", "c2")
                .values(6, "x")
                .exec();

        try (SqlSession session = getSessionManager().openSession()) {

            T3Mapper mapper = session.getMapper(T3Mapper.class);
            Optional<TO3> miss = mapper.find(3L);
            assertFalse(miss.isPresent());

            Optional<TO3> hit = mapper.find(6L);
            assertTrue(hit.isPresent());
            assertEquals(6, hit.get().getC1());
            assertEquals("x", hit.get().getC2());
        }
    }

    @Test
    public void typeHandler() {

        db.getTable("t6").insertColumns("c1", "c2")
                .values(6, 24)
                .exec();

        try (SqlSession session = getSessionManager().openSession()) {

            T6Mapper mapper = session.getMapper(T6Mapper.class);
            Optional<TO6> hit = mapper.find(6L);
            assertTrue(hit.isPresent());
            assertEquals(6, hit.get().getC1());
            assertEquals(24, hit.get().getC2().getV());
        }
    }
}
