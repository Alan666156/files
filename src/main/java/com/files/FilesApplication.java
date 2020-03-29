package com.files;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.files.dao.FileConfigDao;
import com.files.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.Serializable;

/**
 * 启动入口
 * @author fuhx
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class FilesApplication implements CommandLineRunner{
	
	@Autowired
	private FileConfigDao fileConfigDao;
	@Autowired
	private RedisService redisService;
	
	@Bean
	public RedisTemplate<Serializable, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<Serializable, Object> template = new RedisTemplate<Serializable, Object>();
		template.setConnectionFactory(connectionFactory);
		setRedisSerializer(template);
		template.afterPropertiesSet();
		return template;
	}

	private void setRedisSerializer(RedisTemplate<Serializable, ?> template) {
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		template.setKeySerializer(template.getStringSerializer());
		template.setValueSerializer(jackson2JsonRedisSerializer);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(FilesApplication.class, args);
		log.info("=========Files Application Start Success===========");
	}
	
	@Override
	public void run(String... args) throws Exception {
		// 应用启动成功后需要执行的数据
//		Map<String, List<FileConfigInfo>> map = new HashMap<>();
//		List<FileConfig> list = fileConfigDao.findAll();
//		for(FileConfig file : list) {
//			//写入缓存
//			redisService.set(file.getAppId(), file);
//		}
		
	}
}
