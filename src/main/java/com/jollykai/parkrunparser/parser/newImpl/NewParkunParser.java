package com.jollykai.parkrunparser.parser.newImpl;

import com.jollykai.parkrunparser.parser.ParkunParser;
import com.jollykai.parkrunparser.parser.impl.DefaultParkunParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NewParkunParser implements ParkunParser {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultParkunParser.class);

    @Override
    public void parse() throws IOException {
        LOG.debug("you can use this implementation without @Qualifier if you change ParkunParserConfig");
    }
}
