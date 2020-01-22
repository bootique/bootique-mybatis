<!--
  Licensed to ObjectStyle LLC under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ObjectStyle LLC licenses
  this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->

[![Build Status](https://travis-ci.org/bootique/bootique-mybatis.svg)](https://travis-ci.org/bootique/bootique-mybatis)
[![Maven Central](https://img.shields.io/maven-central/v/io.bootique.mybatis/bootique-mybatis.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique.mybatis/bootique-mybatis/)

# bootique-mybatis

Provides [MyBatis](https://mybatis.org/mybatis-3/) integration with [Bootique](https://bootique.io).

*For additional help/questions about this example send a message to
[Bootique forum](https://groups.google.com/forum/#!forum/bootique-user).*

# Setup

## Add bootique-mybatis to your build tool
**Maven**
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>X.X</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependency>
    <groupId>io.bootique.mybatis</groupId>
    <artifactId>bootique-mybatis</artifactId>
</dependency>
