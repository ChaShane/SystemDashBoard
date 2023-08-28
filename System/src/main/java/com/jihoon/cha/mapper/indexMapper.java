package com.jihoon.cha.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface indexMapper {
	public List<Map<String, Object>> dbvar(Map<String, Object> commandMap) throws Exception;
}
