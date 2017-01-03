package com.example.ianribas.mypopularmovies;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSlugify() {
        String url = "http://api.presentation.abc.go.com/api/ws/presentation/v2/module/384272?brand=008&device=009_26&accesslevel=0&type=tilegroup&layouttype=shows";

        String result = url
                .replace("http://", "")
                .replace("abc.go.com", "")
                .replaceAll("[^a-zA-Z0-9_.]+", "-");

        System.out.println("Result: " + result);
        assertThat(result.length(), greaterThan(0));

    }

    @Test
    public void testDateParse() {
        String dateStr = "Tue, 04-Nov-2036 17:22:58 GMT";

        // TODO See the error with new conversion code from Android N (7).
        String[] COOKIE_DATE_FORMATS = {
                "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
                "EEE',' dd MMM yyyy HH:mm:ss 'GMT'",
                "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
                "EEE',' dd-MMM-yy HH:mm:ss 'GMT'",
                "EEE',' dd MMM yy HH:mm:ss 'GMT'",
                "EEE MMM dd yy HH:mm:ss 'GMT'Z"
        };

        TimeZone GMT = TimeZone.getTimeZone("GMT");
        long whenCreated = System.currentTimeMillis();
        Calendar cal = new GregorianCalendar(GMT);
        long maxAge = 0L;
        for (int i = 0; i < COOKIE_DATE_FORMATS.length; i++) {
            SimpleDateFormat df = new SimpleDateFormat(COOKIE_DATE_FORMATS[i],
                    Locale.US);
            cal.set(1970, 0, 1, 0, 0, 0);
            df.setTimeZone(GMT);
            df.setLenient(false);
            df.set2DigitYearStart(cal.getTime());
            try {
                cal.setTime(df.parse(dateStr));
                if (!COOKIE_DATE_FORMATS[i].contains("yyyy")) {
                    // 2-digit years following the standard set
                    // out it rfc 6265
                    int year = cal.get(Calendar.YEAR);
                    year %= 100;
                    if (year < 70) {
                        year += 2000;
                    } else {
                        year += 1900;
                    }
                    cal.set(Calendar.YEAR, year);
                }
                maxAge = (cal.getTimeInMillis() - whenCreated) / 1000;
                break;
            } catch (Exception e) {
                System.out.println("Error parsing with " + COOKIE_DATE_FORMATS[i]);
                e.printStackTrace();
            }
        }
        assertThat(maxAge, not(0L));

    }
}