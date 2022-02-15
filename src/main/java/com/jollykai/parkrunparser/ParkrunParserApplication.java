package com.jollykai.parkrunparser;

import com.jollykai.parkrunparser.parser.ParkunParser;
import com.jollykai.parkrunparser.parser.ParkunParserConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(ParkunParserConfig.class)
@PropertySource({
        "classpath:parkun.properties"
})
public class ParkrunParserApplication {
    @Autowired
    private ParkunParser parser;

    public static void main(String[] args) {
        SpringApplication.run(ParkrunParserApplication.class, args);
    }

    @PostConstruct
    public void runAfterSpringBootStart() throws IOException {
        parser.parse();
    }

}
