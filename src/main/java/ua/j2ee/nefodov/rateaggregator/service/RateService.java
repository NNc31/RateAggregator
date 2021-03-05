package ua.j2ee.nefodov.rateaggregator.service;

import org.springframework.scheduling.annotation.Async;
import ua.j2ee.nefodov.rateaggregator.model.CommonDto;

import java.util.concurrent.Future;

public interface RateService {

    String getName();

    @Async
    Future<CommonDto> getCommonDto(String date, String currCode);
}
