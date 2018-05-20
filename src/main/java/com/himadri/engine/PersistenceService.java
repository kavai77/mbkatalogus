package com.himadri.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.himadri.model.service.InstanceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;

@Service
public class PersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceService.class);


    @Value("${persistenceLocation}")
    private String persistenceLocation;

    @Value("${instance}")
    private String instance;

    @Autowired
    private ObjectMapper objectMapper;

    private File jsonFile;

    private InstanceProperties instanceProperties;

    @PostConstruct
    public void init() {
        jsonFile = new File(persistenceLocation, instance + ".json");
        try (Reader reader = new FileReader(jsonFile)) {
            instanceProperties = objectMapper.readValue(reader, InstanceProperties.class);
        } catch (IOException e) {
            instanceProperties = new InstanceProperties();
        }

    }

    public InstanceProperties getInstanceProperties() {
        return instanceProperties;
    }

    public void persistInstanceProperties() {
        try (Writer writer = new FileWriter(jsonFile)) {
            objectMapper.writeValue(writer, instanceProperties);
        } catch (IOException e) {
            LOGGER.error("Could not store instanceProperties", e);
        }
    }
}
