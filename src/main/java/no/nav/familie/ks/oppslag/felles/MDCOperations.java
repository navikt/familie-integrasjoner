package no.nav.familie.ks.oppslag.felles;

import org.slf4j.MDC;

import javax.xml.namespace.QName;
import java.util.Objects;
import java.util.Random;

import static org.slf4j.MDC.get;
import static org.slf4j.MDC.put;

/**
 * Utility-klasse for kommunikasjon med MDC.
 * (Knabbet fra modig-log-common)
 */
public final class MDCOperations {
    public static final String HTTP_HEADER_CALL_ID = "Nav-Callid";
    public static final String HTTP_HEADER_CONSUMER_ID = "Nav-Consumer-Id";

    public static final String MDC_CALL_ID = "callId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_CONSUMER_ID = "consumerId";

    // QName for the callId header
    public static final QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", MDC_CALL_ID);

    private static final Random RANDOM = new Random();

    private MDCOperations() {
    }

    public static void putCallId() {
        putCallId(generateCallId());
    }

    public static void putCallId(String callId) {
        Objects.requireNonNull(callId, "callId can't be null");
        put(MDC_CALL_ID, callId);
    }

    public static String getCallId() {
        return get(MDC_CALL_ID);
    }

    public static void removeCallId() {
        remove(MDC_CALL_ID);
    }

    public static void putConsumerId(String consumerId) {
        Objects.requireNonNull(consumerId, "consumerId can't be null");
        put(MDC_CONSUMER_ID, consumerId);
    }

    public static String getConsumerId() {
        return get(MDC_CONSUMER_ID);
    }
    public static void removeConsumerId() {
        remove(MDC_CONSUMER_ID);
    }

    public static void putUserId(String userId) {
        Objects.requireNonNull(userId, "userId can't be null");
        put(MDC_USER_ID, userId);
    }

    public static String getUserId() {
        return get(MDC_USER_ID);
    }

    public static void removeUserId() {
        remove(MDC_USER_ID);
    }

    public static String generateCallId() {
        int randomNr = RANDOM.nextInt(Integer.MAX_VALUE);
        long systemTime = System.currentTimeMillis();

        StringBuilder callId = new StringBuilder("CallId_")
                .append(systemTime)
                .append('_')
                .append(randomNr);

        return callId.toString();
    }

    public static String getFromMDC(String key) {
        String value = MDC.get(key);
        return value;
    }

    public static void putToMDC(String key, String value) {
        put(key, value);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

}