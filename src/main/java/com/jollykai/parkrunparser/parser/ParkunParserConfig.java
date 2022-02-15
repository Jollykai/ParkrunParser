package com.jollykai.parkrunparser.parser;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan("com.jollykai.parkrunparser.parser.impl")
@PropertySource("classpath:parkun.properties")
public class ParkunParserConfig {
}
