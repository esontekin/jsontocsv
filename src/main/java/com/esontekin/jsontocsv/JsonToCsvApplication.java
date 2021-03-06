package com.esontekin.jsontocsv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

@SpringBootApplication
public class JsonToCsvApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonToCsvApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JsonToCsvApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            LOGGER.info(EmojiParser.parseToUnicode( ":rocket: Starting file discovery!"));
            out.print(EmojiParser.parseToUnicode(":steam_locomotive: Enter the file path : "));
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine().trim();
            Path path = Paths.get(line);
            out.print("Enter output directory : ");
            String output = scanner.nextLine().trim();
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("Path must be a directory");
            }
            out.println(ConsoleColors.BLUE + "Checking output directory " + ConsoleColors.RESET);
            Path outputPath = Paths.get(output);
            if(Files.notExists(outputPath)){
                Files.createDirectory(outputPath);
                out.println(ConsoleColors.GREEN + "Output directory created! " + ConsoleColors.RESET + "\u2728");
            }
            out.println(ConsoleColors.GREEN + "Output directory ok! " + ConsoleColors.RESET + "\uD83D\uDC7D" );
            List<String> filesPath;
            try (Stream<Path> walk = Files.walk(path)){
                filesPath = walk
                        .filter(p -> !Files.isDirectory(p))
                        .map(Path::toString)
                        .filter(f -> f.endsWith("json"))
                        .collect(Collectors.toList());
            }
            for (String s : filesPath) {
                out.println(ConsoleColors.YELLOW + s + " opened" + ConsoleColors.RESET +" \uD83D\uDE00");
                Map<String, Object> result = new ObjectMapper().readValue(new File(s), HashMap.class);
                if (!result.containsKey("intId") || !result.containsKey("extId") || !result.containsKey("name"))
                    continue;
                String fileName =
                        result.get("intId").toString() + "-" +
                        result.get("extId").toString() + "-" +
                        result.get("name").toString() + ".csv";
                List<Object> users = Collections.singletonList(result.get("users"));
                BufferedWriter writer = new BufferedWriter(new FileWriter(output + "/" + fileName));
                writer.write("Toplant??_ID | Ders_Ad?? | D??nem_Hafta | Ad_Soyad | ????retim_Eleman??_m?? | Cevaplar " +
                        "| Toplam_Konu??ma_S??resi | En_Son_Konu??man??n_Ba??lad??????_Zaman | Kameran??n_A????ld??????_Zaman | Kameran??n_Durdu??u_Zaman | Toplam_Mesaj_Say??s??");
                writer.newLine();
                for(Object user : users) {
                    Map<String, Object> userMap = (Map<String, Object>) user;
                    for (Object value : userMap.values()) {
                        Map<String, Object> userValueMap = (Map<String, Object>)value;
                        writer.write(result.get("intId").toString() + "|");
                        writer.write(result.get("extId").toString() + "|");
                        writer.write(result.get("name").toString() + "|");
                        writer.write(userValueMap.get("name").toString() + "|");
                        writer.write(userValueMap.get("isModerator").toString() + "|");
                        writer.write(userValueMap.containsKey("answers") ? userValueMap.get("answers").toString() : "");
                        writer.write( "|");
                        Map<String, Object> talkMap = (Map<String, Object>)userValueMap.get("talk");
                        writer.write(talkMap.get("totalTime").toString());
                        writer.write( "|");
                        String lastTalkStartedOnString = "";
                        Date expirylastTalkStartedOn;
                        if (!"0".equals(talkMap.get("lastTalkStartedOn").toString())) {
                            long epochlastTalkStartedOn = Long.parseLong(talkMap.get("lastTalkStartedOn").toString());
                            expirylastTalkStartedOn = new Date( epochlastTalkStartedOn  );
                            lastTalkStartedOnString = formatter.format(expirylastTalkStartedOn);
                        }
                        writer.write(lastTalkStartedOnString);
                        writer.write( "|");
                        String webcamStart = "";
                        String webcamStop = "";
                        ArrayList webcamsList = (ArrayList) userValueMap.get("webcams");
                        for (Object webcamLinkList : webcamsList) {
                            Map<String, Object> webcamsMap = (Map<String, Object>) webcamLinkList;
                            long epoch = Long.parseLong( webcamsMap.get("startedOn").toString() );
                            Date expiry = new Date( epoch  );
                            webcamStart += formatter.format(expiry) + " , ";
                            epoch = Long.parseLong( webcamsMap.get("stoppedOn").toString() );
                            expiry = new Date( epoch );
                            webcamStop += formatter.format(expiry) + " , ";

                        }

                        writer.write(webcamStart);
                        writer.write( "|");
                        writer.write(webcamStop);
                        writer.write( "|");
                        writer.write(userValueMap.containsKey("totalOfMessages") ? userValueMap.get("totalOfMessages").toString() : "");
                        writer.newLine();
                    }
                }
                writer.close();
                out.println(ConsoleColors.GREEN + s + " done!" + ConsoleColors.RESET + " \uD83D\uDE0E");
            }

        };
    }
}
