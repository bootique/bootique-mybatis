package io.bootique.mybatis.testmappers3;

import io.bootique.mybatis.testpojos.TO7;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface T7Mapper {

    Optional<TO7> find(Long c1);
}
