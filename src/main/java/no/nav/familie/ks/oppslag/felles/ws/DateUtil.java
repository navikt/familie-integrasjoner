package no.nav.familie.ks.oppslag.felles.ws;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

    private static final DatatypeFactory DATATYPE_FACTORY;
    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private DateUtil() {
    }

    public static XMLGregorianCalendar convertToXMLGregorianCalendar(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        return DATATYPE_FACTORY.newXMLGregorianCalendar(gregorianCalendar);
    }

    public static XMLGregorianCalendar convertToXMLGregorianCalendar(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        return DATATYPE_FACTORY.newXMLGregorianCalendar(gregorianCalendar);
    }

    public static XMLGregorianCalendar convertToXMLGregorianCalendarRemoveTimezone(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return DATATYPE_FACTORY.newXMLGregorianCalendar(
            localDate.getYear(),
            localDate.getMonthValue(),
            localDate.getDayOfMonth(),
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED);
    }

    public static LocalDateTime convertToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        }
        return LocalDateTime.ofInstant(xmlGregorianCalendar.toGregorianCalendar().getTime().toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate convertToLocalDate(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        }
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }
}