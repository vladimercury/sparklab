package com.mercury;

import org.apache.spark.api.java.function.Function;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogEntry implements Serializable {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss");
    private static final String BLOCK_DELIMITER = "\"";
    private static final String WORD_DELIMITER = " ";

    private String url;
    private Date date;
    private String method;
    private Integer code;

    public LogEntry() {

    }

    private LogEntry(String url, Date date, String method, int code) {
        this.url = url;
        this.date = date;
        this.method = method;
        this.code = code;
    }

    public static LogEntry fromString(String s) {
        try {
            String[] blocks = s.split(BLOCK_DELIMITER);
            String[] hostDateBlock = blocks[0].split(WORD_DELIMITER);
            String[] requestBlock = blocks[1].split(WORD_DELIMITER);
            String[] replyBlock = blocks[2].split(WORD_DELIMITER);

            String url = hostDateBlock[0];
            String dateString = hostDateBlock[3].substring(1);
            String method = requestBlock[0];
            String codeString = replyBlock[1];

            Date date = new Date(LocalDateTime.parse(dateString, FORMATTER)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
            return new LogEntry(url, date, method, Integer.parseInt(codeString));
        } catch (Exception e) {
            System.out.println(String.format("Error while parsing '%s': %s", s, e.getMessage()));
        }
        return null;
    }

    public static Function<String, LogEntry> fromString = LogEntry::fromString;

    public String getUrl() {
        return url;
    }

    public Date getDate() {
        return date;
    }

    public String getMethod() {
        return method;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String toString() {
        return String.format("LOG ENTRY: '%s' '%s' '%s' '%d'\n",
                this.url, this.date.toString(), this.method, this.code);
    }
}
