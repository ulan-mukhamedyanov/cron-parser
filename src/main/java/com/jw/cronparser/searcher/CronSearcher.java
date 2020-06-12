package com.jw.cronparser.searcher;

import java.time.LocalDateTime;

public interface CronSearcher {

    LocalDateTime findClosest(LocalDateTime dateTime);

}
