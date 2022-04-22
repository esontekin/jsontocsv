package com.esontekin.jsontocsv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class JsonToCsvApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonToCsvApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JsonToCsvApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() throws Exception {
        return args -> {
            LOGGER.info("Starting file discovery!");
            System.out.print("Enter the file path : ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            Path path = Paths.get(line);
            System.out.print("Enter output directory : ");
            String output = scanner.nextLine();
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("Path must be a directory");
            }
            List<String> filesPath = Lists.newArrayList();
            try (Stream<Path> walk = Files.walk(path)){
                filesPath = walk
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.toString())
                        .filter(f -> f.endsWith("json"))
                        .collect(Collectors.toList());
            }
            for (String s : filesPath) {
                System.out.println(ConsoleColors.YELLOW + s + " opened" + ConsoleColors.RESET +" \uD83D\uDE00");
                Map<String, Object> result = new ObjectMapper().readValue(new File(s), HashMap.class);

                System.out.println(ConsoleColors.GREEN + s + " done!" + ConsoleColors.RESET + " \uD83D\uDE0E");
            }

        };
    }
}
