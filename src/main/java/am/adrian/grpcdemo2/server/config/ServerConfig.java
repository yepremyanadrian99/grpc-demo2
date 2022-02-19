package am.adrian.grpcdemo2.server.config;

import am.adrian.grpcdemo2.server.MainApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = MainApplication.class)
public class ServerConfig {
}
