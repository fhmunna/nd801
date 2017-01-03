package com.example.ianribas.mypopularmovies;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Testing HttpCookie.parse() behavior on Android N.
 */

@RunWith(AndroidJUnit4.class)
public class CookieParseTest {
    @Test
    public void testCookieParse() {
        String hAnd7 = "Set-Cookie: SWID=5F51247E-6603-4A82-8351-E8CF0DD8FDE9; path=/; expires=Fri, 04-Nov-2036 17:22:58 GMT; domain=.go.com;\n";
        String hAnd5 = "Set-Cookie: SWID=9BFF6E2F-9C08-4E4E-A24A-CCE0A3D97090; path=/; expires=Fri, 04-Nov-2036 18:06:31 GMT; domain=.go.com;\n";

        final HttpCookie cookie = HttpCookie.parse(hAnd7).get(0);
        assertThat(cookie.getVersion(), is(0));
        assertThat(cookie.getValue(), is("5F51247E-6603-4A82-8351-E8CF0DD8FDE9"));
        assertThat(cookie.toString(), containsString("SWID=5F51247E-6603-4A82-8351-E8CF0DD8FDE9"));

        final HttpCookie cookie1 = HttpCookie.parse(hAnd5).get(0);
        assertThat(cookie1.getVersion(), is(0));
        assertThat(cookie1.getValue(), is("9BFF6E2F-9C08-4E4E-A24A-CCE0A3D97090"));
        assertThat(cookie1.toString(), containsString("9BFF6E2F-9C08-4E4E-A24A-CCE0A3D97090"));

    }

    @Test
    public void testCookieManager() throws IOException, URISyntaxException {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
//        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        assertFalse(cookieManager.getCookieStore() == null);

        URI uri = new URI("http://api.config.watchabc.go.com/vp2/ws/s/config/2024/009/009_27/Android%20SDK%20built%20for%20x86");
        Map<String, List<String>> headers = Collections.singletonMap("Set-Cookie", Collections.singletonList("SWID=5F51247E-6603-4A82-8351-E8CF0DD8FDE9; path=/; expires=Fri, 04-Nov-2036 17:22:58 GMT; domain=.go.com;\n"));
        cookieManager.put(uri, headers);

        assertThat(cookieManager.getCookieStore().getCookies().size(), is(1));

        Map<String, List<String>> cookies = cookieManager.get(new URI("http://api.ads.watchabc.go.com/vp2/ws/ads/2015/lf"), new HashMap<String, List<String>>());
//        Map<String, List<String>> cookies = cookieManager.get(new URI("http://api.config.watchabc.go.com/vp2/ws/s/config/2024/009/009_27/Android%20SDK%20built%20for%20x86"), new HashMap<String, List<String>>());

        assertThat(cookies.get("Cookie").get(0), containsString("SWID=5F51247E-6603-4A82-8351-E8CF0DD8FDE9"));

    }
}
