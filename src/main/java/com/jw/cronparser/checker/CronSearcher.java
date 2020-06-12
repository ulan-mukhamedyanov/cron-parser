package com.jw.cronparser.checker;

import java.time.LocalDateTime;

public interface CronSearcher {

    LocalDateTime findClosest(LocalDateTime dateTime);

}
