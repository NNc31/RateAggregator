package ua.j2ee.nefodov.rateaggregator.service;

import ua.j2ee.nefodov.rateaggregator.model.Rate;

import java.util.List;

public interface FileCreatorService {

    Object createFile(List<Rate> rateList);

    byte[] fileToBytes(Object file);
}
