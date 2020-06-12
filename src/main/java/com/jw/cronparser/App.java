package com.jw.cronparser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.jw.cronparser.domain.CronObject;

public class App {

    private static final String EXPRESSION = "0 0 0 12,13,16 * ? *";

    public static void main(String[] args) {
        CronParser parser = new CronParser();
        CronObject object = parser.parse(EXPRESSION);
        System.out.println(object.previousFireDateTime(LocalDateTime.now()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

}
