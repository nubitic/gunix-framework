package mx.com.gunix.framework.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class DozerConfig {
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	@Bean
	public DozerBeanMapper getDozerBeanMapper() throws IOException {
		DozerBeanMapper dzm = new DozerBeanMapper();
		Optional<Resource[]> resources = Optional.ofNullable(resourcePatternResolver.getResources("classpath*:/mx/com/gunix/framework/dozerMappings/*.xml"));
		
		resources.ifPresent(ress->{
			List<String> mappingsList = new ArrayList<String>();
			for(Resource res:ress){
				if(res instanceof FileSystemResource){
					try {
						mappingsList.add(((FileSystemResource)res).getURL().toString());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}else{
					mappingsList.add(res.toString());
					}
			}
			dzm.setMappingFiles(mappingsList);
		});
		return dzm;
	}
}
