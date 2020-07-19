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
import io.bootique.mybatis.testmappers2.T2Mapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@BQTest
public class MybatisModuleTxIT {

    @BQTestTool
    static final DerbyTester db = DerbyTester
            .db()
            .deleteBeforeEachTest("t2");

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app()
            .autoLoadModules()
            .module(db.moduleWithTestDataSource("db"))
            .module(b -> MybatisModule.extend(b).addMapper(T2Mapper.class))
            .createRuntime();

    static SqlSessionManager getSessionManager() {
        return app.getInstance(SqlSessionManager.class);
    }

    @BeforeAll
    static void createSchema() {
        db.execStatement().exec("CREATE TABLE \"t2\" (\"c1\" INT, \"c2\" VARCHAR(10))");
    }

    @Test
    public void testCommit() {

        try (SqlSession session = getSessionManager().openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
            session.commit();
        }

        db.getTable("t2").matcher().eq("c1", 1L).assertOneMatch();
    }

    @Test
    public void testMapperOutsideSession_ImplicitAutocommit() {

        T2Mapper mapper = getSessionManager().getMapper(T2Mapper.class);
        mapper.insert(1L, "one");
        db.getTable("t2").matcher().eq("c1", 1L).assertOneMatch();
    }

    @Test
    public void testRollback() {

        try (SqlSession session = getSessionManager().openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
            session.rollback();
        }

        db.getTable("t2").matcher().eq("c1", 1L).assertNoMatches();
    }

    @Test
    public void testNoCommit() {

        try (SqlSession session = getSessionManager().openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
        }

        db.getTable("t2").matcher().eq("c1", 1L).assertNoMatches();
    }

    @Test
    public void testCommitMultiple() {

        try (SqlSession session = getSessionManager().openSession()) {
            T2Mapper mapper = session.getMapper(T2Mapper.class);
            mapper.insert(1L, "one");
            mapper.insert(2L, "two");
            session.commit();
        }

        db.getTable("t2").matcher().eq("c1", 1L).assertOneMatch();
        db.getTable("t2").matcher().eq("c1", 2L).assertOneMatch();
    }
}
