package ua.j2ee.nefodov.rateaggregator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParser {

    private static final Logger logger = LoggerFactory.getLogger(DateParser.class);

    private static final DateTimeFormatter COMMON_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter NBU_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter PRIVAT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final LocalDate MAX_DATE = LocalDate.of(2012, 1, 30);

    public static String parseToNbu(String dateStr) {
        logger.debug("Parsing date to NBU format");
        LocalDate date = LocalDate.parse(dateStr, COMMON_FORMAT);
        return date.format(NBU_FORMAT);
    }

    public static String parseToPrivat(String dateStr) {
        logger.debug("Parsing date to Privat format");
        LocalDate date = LocalDate.parse(dateStr, COMMON_FORMAT);
        return date.format(PRIVAT_FORMAT);
    }

    public static String parseToCommon(LocalDate date) {
        logger.debug("Parsing date to common format");
        return date.format(COMMON_FORMAT);
    }

    public static String nowCommonFormat() {
        logger.debug("Returning present day in common format");
        return LocalDate.now().format(COMMON_FORMAT);
    }

    public static boolean validateDate(String dateStr) {
        logger.debug("Validating date " + dateStr);
        try {
            LocalDate date = LocalDate.parse(dateStr, COMMON_FORMAT);
            logger.debug("Date parsed");
            return !date.isBefore(MAX_DATE) && !date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            logger.info("Caught parse exception for " + dateStr);
            return false;
        }
    }

    public static LocalDate parseStartDate(String periodStr) {
        logger.debug("Parsing start date from " + periodStr);
        LocalDate startDate;
        if (periodStr.contains("-")) {
            try {
                startDate = LocalDate.parse(periodStr.substring(0, periodStr.indexOf('-')), COMMON_FORMAT);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid period");
            }
        } else {
            switch (periodStr) {
                case "week":
                    startDate = LocalDate.now().minusWeeks(1);
                    break;
                case "month":
                    startDate = LocalDate.now().minusMonths(1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid period");
            }
        }
        logger.debug("Start date parsed as " + startDate);
        return startDate;
    }

    public static LocalDate parseEndDate(String periodStr) {
        logger.debug("Parsing end date from " + periodStr);
        LocalDate endDate;
        if (periodStr.contains("-")) {
            try {
                endDate = LocalDate.parse(periodStr.substring(periodStr.indexOf('-') + 1), COMMON_FORMAT);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid period");
            }
        } else {
            endDate = LocalDate.now();
        }
        logger.debug("End date parsed as " + endDate);
        return endDate;
    }

    public static boolean validatePeriod(String periodStr) {
        logger.debug("Validating period " + periodStr);
        if (periodStr.contains("-")) {
            try {
                LocalDate startDate = LocalDate.parse(periodStr.substring(0, periodStr.indexOf('-')), COMMON_FORMAT);
                LocalDate endDate = LocalDate.parse(periodStr.substring(periodStr.indexOf('-') + 1), COMMON_FORMAT);
                Period period = Period.between(startDate, endDate);
                return startDate.isBefore(endDate) && startDate.isAfter(MAX_DATE) &&
                        !endDate.isAfter(LocalDate.now()) && period.getYears() == 0;
            } catch (DateTimeParseException e) {
                logger.info("Caught parse exception for " + periodStr);
                return false;
            }
        } else {
            return periodStr.equals("week") || periodStr.equals("month");
        }
    }
}
