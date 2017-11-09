package com.lmu.settleBattle.server;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@EnableAutoConfiguration
@SpringBootApplication
public class ServerApp extends SpringBootServletInitializer{

  private static Class applicationClass = Application.class;

  public static void main(String[] args) {
		SpringApplication.run(ServerApp.class, args);
	}
}
