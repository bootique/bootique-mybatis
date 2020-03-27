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

import io.bootique.ModuleExtender;
import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.di.SetBuilder;
import io.bootique.di.TypeLiteral;
import org.apache.ibatis.type.TypeHandler;

public class MyBatisModuleExtender extends ModuleExtender<MyBatisModuleExtender> {

    private SetBuilder<Class<?>> mappers;
    private SetBuilder<Package> mapperPackages;
    private SetBuilder<TypeHandler> handlers;

    public MyBatisModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public MyBatisModuleExtender initAllExtensions() {
        contributeMappers();
        contributeMapperPackages();
        contributeTypeHandlers();
        return this;
    }

    public MyBatisModuleExtender addMapper(Class<?> mapperType) {
        contributeMappers().add(mapperType);
        return this;
    }

    public MyBatisModuleExtender addMapperPackage(Package aPackage) {
        contributeMapperPackages().add(aPackage);
        return this;
    }

    public MyBatisModuleExtender addMapperPackage(Class<?> anyClassInPackage) {
        contributeMapperPackages().add(anyClassInPackage.getPackage());
        return this;
    }

    public <T extends TypeHandler> MyBatisModuleExtender addTypeHandler(Class<T> handlerType) {
        contributeTypeHandlers().add(handlerType);
        return this;
    }

    public <T extends TypeHandler> MyBatisModuleExtender addTypeHandler(T handler) {
        contributeTypeHandlers().add(handler);
        return this;
    }

    protected SetBuilder<Class<?>> contributeMappers() {
        if (mappers == null) {
            TypeLiteral<Class<?>> type = new TypeLiteral<Class<?>>() {
            };
            mappers = newSet(Key.get(type, ByMybatisModule.class));
        }
        return mappers;
    }

    protected SetBuilder<Package> contributeMapperPackages() {
        if (mapperPackages == null) {
            mapperPackages = newSet(Package.class, ByMybatisModule.class);
        }
        return mapperPackages;
    }

    protected SetBuilder<TypeHandler> contributeTypeHandlers() {
        if (handlers == null) {
            handlers = newSet(TypeHandler.class);
        }
        return handlers;
    }
}
