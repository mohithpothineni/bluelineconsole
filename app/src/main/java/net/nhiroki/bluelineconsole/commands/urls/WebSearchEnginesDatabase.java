package net.nhiroki.bluelineconsole.commands.urls;

import android.content.Context;
import android.util.Pair;

import net.nhiroki.bluelineconsole.dataStore.persistent.URLEntry;
import net.nhiroki.bluelineconsole.dataStore.persistent.URLPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WebSearchEnginesDatabase {
    private static final Set<String> WIKIPEDIA_SUPPORTING_LANGS = new HashSet<>(
            Arrays.asList(
                    // List as of 2018/03/10, from https://www.wikipedia.org/
                    // TODO: processing domains with hyphen

                    // 1 000 000+ articles
                    "de", "en", "es", "fr", "it", "nl", "ja", "pl", "ru", "ceb", "sv", "vi", "war",

                    // 100 000+ articles
                    "ar", "az", "bg", "zh-min-nan", "be", "ca", "cs", "da", "et", "el", "eo", "eu", "fa", "gl", "ko",
                    "hy", "hi", "hr", "id", "he", "ka", "la", "lt", "hu", "ms", "min", "no", "ce", "uz", "pt", "kk",
                    "ro", "simple", "sk", "sl", "sr", "sh", "fi", "ta", "th", "tr", "uk", "ur", "vo", "zh",

                    // 10 000+ articles
                    "af", "als", "am", "an", "ast", "bn", "map-bms", "ba", "be-tarask", "bpy", "bar", "bs", "br", "cv",
                    "fo", "fy", "ga", "gd", "gu", "hsb", "io", "ilo", "ia", "os", "is", "jv", "kn", "ht", "ku", "ckb",
                    "ky", "mrj", "lv", "lb", "li", "lmo", "mai", "mk", "mg", "ml", "mr", "xmf", "arz", "mzn", "cdo", "mn",
                    "my", "new", "ne", "nap", "oc", "or", "pa", "pnb", "pms", "nds", "qu", "cy", "sa", "sah", "sco", "sq",
                    "scn", "si", "su", "sw", "tl", "tt", "te", "tg", "azb", "bug", "vec", "wa", "yi", "yo", "zh-yue", "bat-smg",

                    // 1 000+ articles
                    "ace", "kbd", "ang", "ab", "roa-rup", "frp", "arc", "gn", "av", "ay", "bjn", "bh", "bcl", "bi", "bo", "bxr",
                    "cbk-zam", "co", "za", "se", "pdc", "dv", "nv", "dsb", "eml", "myv", "ext", "hif", "fur", "gv", "gag", "ki",
                    "glk", "gan", "hak", "xal", "ha", "haw", "ig", "ie", "kl", "pam", "csb", "kw", "km", "rw", "kv", "kg", "gom",
                    "lo", "lad", "lbe", "lez", "lij", "ln", "jbo", "lrc", "lg", "mt", "zh-classical", "ty", "mi", "mwl", "mdf",
                    "nah", "na", "nds-nl", "frr", "nrm", "nov", "mhr", "as", "pi", "pag", "pap", "ps", "koi", "pfl", "pcd",
                    "krc", "kaa", "crh", "ksh", "rm", "rue", "sc", "stq", "nso", "sn", "sd", "szl", "so", "srn", "kab", "roa-tara",
                    "tet", "tpi", "to", "tk", "tyv", "udm", "ug", "vep", "fiu-vro", "vls", "wo", "wuu", "diq", "zea",

                    // 100+ articles
                    "ak", "bm", "ch", "ny", "ee", "ff", "got", "iu", "ik", "ks", "ltg", "fj", "cr", "pih", "om", "pnt", "dz", "rmy",
                    "rn", "sm", "sg", "st", "tn", "cu", "ss", "ti", "chr", "chy", "ve", "ts", "tum", "tw", "xh", "zu"
            )
    );

    // key -> pair(engine name, search base URL)
    // Most keys are country code, but some exceptions exist.
    private static final Map<String, Pair<String, String>> YAHOO_SEARCH_IN_THE_WORLD = new HashMap<String, Pair<String, String>>() {
        {
            // List is created on 2018/03/10. I picked up domains with an independent search engine page from https://www.yahoo.com/everything/world/ .
            // I tried to write as correct title as possible mostly by reading title of top page and search result page.
            // If you find anything wrong in this list, your pull request is welcome.

            // qc and espanol must be selected by manual specification by caller

            // Québec state in Canada. Specify if locale is français (Canada) : French (Canada).
            put("qc", new Pair<String, String>("Yahoo", "https://qc.search.yahoo.com/search?p="));
            // Manually specify if locale is español (Estados Unidos) : Spanish (United States).
            put("espanol", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));

            // by country code
            put("AR", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("BR", new Pair<String, String>("Yahoo", "https://br.search.yahoo.com/search?p="));
            put("CA", new Pair<String, String>("Yahoo Canada", "https://ca.search.yahoo.com/search?p="));
            put("CL", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("DE", new Pair<String, String>("Yahoo", "https://de.search.yahoo.com/search?p="));
            put("FR", new Pair<String, String>("Yahoo", "https://fr.search.yahoo.com/search?p="));
            put("HK", new Pair<String, String>("Yahoo雅虎香港", "https://hk.search.yahoo.com/search?p="));
            put("ID", new Pair<String, String>("Yahoo", "https://id.search.yahoo.com/search?p="));
            put("IE", new Pair<String, String>("Yahoo", "https://uk.search.yahoo.com/search?p="));
            put("IN", new Pair<String, String>("Yahoo India", "https://in.search.yahoo.com/search?p="));
            put("IT", new Pair<String, String>("Yahoo Italia", "https://it.search.yahoo.com/search?p="));
            // Despite Yahoo! JAPAN is not listed on https://www.yahoo.com/everything/world/ , Yahoo! JAPAN has the license to use the name.
            // See: https://en.wikipedia.org/wiki/Yahoo%21_Japan (2018/03/10)
            put("JP", new Pair<String, String>("Yahoo! JAPAN", "https://search.yahoo.co.jp/search?p="));
            put("MX", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("MY", new Pair<String, String>("Yahoo Malaysia", "https://malaysia.search.yahoo.com/search?p="));
            put("PE", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("PH", new Pair<String, String>("Yahoo", "https://ph.search.yahoo.com/search?p="));
            put("SE", new Pair<String, String>("Yahoo", "https://se.search.yahoo.com/search?p="));
            put("SG", new Pair<String, String>("Yahoo", "https://sg.search.yahoo.com/search?p="));
            put("TW", new Pair<String, String>("Yahoo奇摩", "https://tw.search.yahoo.com/search?p="));
            put("UK", new Pair<String, String>("Yahoo", "https://uk.search.yahoo.com/search?p="));
            put("US", new Pair<String, String>("Yahoo", "https://search.yahoo.com/search?p="));
            put("VE", new Pair<String, String>("Yahoo", "https://espanol.search.yahoo.com/search?p="));
            put("VN", new Pair<String, String>("Yahoo", "https://vn.search.yahoo.com/search?p="));
            put("ZA", new Pair<String, String>("Yahoo", "https://uk.search.yahoo.com/search?p="));
        }
    };

    private List<URLEntry> _customSearches;
    private List<URLEntry> _customStaticURLs;
    private Context _context;

    public WebSearchEnginesDatabase(Context context) {
        this._customSearches = new ArrayList<>();
        this._customStaticURLs = new ArrayList<>();
        this._context = context;

        this.refresh();
    }

    public void refresh() {
        List<URLEntry> entries = URLPreferences.getInstance(this._context).getAllEntries();
        this._customSearches.clear();
        this._customStaticURLs.clear();

        for (URLEntry e : entries) {
            if (e.has_query) {
                this._customSearches.add(e);
            } else {
                this._customStaticURLs.add(e);
            }
        }
    }

    public List<Pair<String, String>> getEngineConfigList() {
        List<Pair<String, String>> ret = new ArrayList<>();

        for (URLEntry e : this._customSearches) {
            ret.add(new Pair<String, String>("custom-web-" + String.valueOf(e.id), e.display_name));
        }

        ret.add(new Pair<String, String>("default-web-wikipedia", "Wikipedia"));
        ret.add(new Pair<String, String>("default-web-wikipedia-en", "Wikipedia (English)"));
        ret.add(new Pair<String, String>("default-web-duckduckgo", "DuckDuckGo"));
        ret.add(new Pair<String, String>("default-web-bing", "Bing"));
        ret.add(new Pair<String, String>("default-web-bing-en", "Bing (English)"));
        ret.add(new Pair<String, String>("default-web-yahoo", "Yahoo"));
        ret.add(new Pair<String, String>("default-web-yahoo-en-us", "Yahoo (English, US)"));
        ret.add(new Pair<String, String>("default-web-google", "Google"));
        ret.add(new Pair<String, String>("default-web-google-en", "Google (English)"));

        return ret;
    }

    public WebSearchEngine getEngineByPreference(String pref_str, Locale locale) {
        if (pref_str == null) {
            return null;
        }

        if (pref_str.equals("none")) {
            return null;
        }

        for (URLEntry e : this._customSearches) {
            if (pref_str.equals("custom-web-" + String.valueOf(e.id))) {
                return new WebSearchEngine(e.display_name, e.url_base);
            }
        }

        if (pref_str.equals("default-web-wikipedia")) {
            String langCode = locale.getLanguage();
            if (!WIKIPEDIA_SUPPORTING_LANGS.contains(langCode)) {
                langCode = "en";
            }
            return new WebSearchEngine("Wikipedia (" + locale.getLanguage() + ")", "https://" + locale.getLanguage() + ".wikipedia.org/wiki/");
        }

        if (pref_str.equals("default-web-wikipedia-en")) {
            return new WebSearchEngine("Wikipedia (en)", "https://en.wikipedia.org/wiki/");
        }

        if (pref_str.equals("default-web-duckduckgo")) {
            return new WebSearchEngine("DuckDuckGo", "https://duckduckgo.com/?q=");
        }

        if (pref_str.equals("default-web-bing")) {
            return new WebSearchEngine("Bing", "https://www.bing.com/search?q=");
        }

        if (pref_str.equals("default-web-bing-en")) {
            return new WebSearchEngine("Bing (English, US)", "https://www.bing.com/search?setlang=en-us&q=");
        }

        if (pref_str.equals("default-web-yahoo")) {
            String countryCode = locale.getCountry();
            String localeAsString = locale.toString();

            if (localeAsString.equals("fr_CA")) {
                countryCode = "qc";
            }
            if (localeAsString.equals("es_US")) {
                countryCode = "espanol";
            }


            if (!YAHOO_SEARCH_IN_THE_WORLD.containsKey(countryCode)) {
                countryCode = "US";
            }
            return new WebSearchEngine(YAHOO_SEARCH_IN_THE_WORLD.get(countryCode).first, YAHOO_SEARCH_IN_THE_WORLD.get(countryCode).second);
        }

        if (pref_str.equals("default-web-yahoo-en-us")) {
            return new WebSearchEngine("Yahoo (United States)", YAHOO_SEARCH_IN_THE_WORLD.get("US").second);
        }

        if (pref_str.equals("default-web-google")) {
            return new WebSearchEngine("Google", "https://www.google.com/search?q=");
        }

        if (pref_str.equals("default-web-google-en")) {
            return new WebSearchEngine("Google (English)", "https://www.google.com/search?hl=en&q=");
        }

        return null;
    }

    public List<WebSearchEngine> getEngineListByNameQuery(String engine, Locale locale) {
        List<WebSearchEngine> ret = new ArrayList<>();

        for (URLEntry e : this._customSearches) {
            if (e.name.startsWith(engine)) {
                ret.add(new WebSearchEngine(e.display_name, e.url_base));
            }
        }

        if ("wikipedia".startsWith(engine)) {
            String langCode = locale.getLanguage();
            if (!WIKIPEDIA_SUPPORTING_LANGS.contains(langCode)) {
                langCode = "en";
            }
            ret.add(new WebSearchEngine("Wikipedia (" + locale.getLanguage() + ")", "https://" + locale.getLanguage() + ".wikipedia.org/wiki/"));
            ret.add(new WebSearchEngine("Wikipedia (en)", "https://en.wikipedia.org/wiki/"));
        }

        if ("duckduckgo".startsWith(engine)) {
            ret.add(new WebSearchEngine("DuckDuckGo", "https://duckduckgo.com/?q="));
        }

        if ("bing".startsWith(engine)) {
            ret.add(new WebSearchEngine("Bing", "https://www.bing.com/search?q="));
            ret.add(new WebSearchEngine("Bing (English, US)", "https://www.bing.com/search?setlang=en-us&q="));
        }

        if ("yahoo".startsWith(engine)) {
            String countryCode = locale.getCountry();
            String localeAsString = locale.toString();

            if (localeAsString.equals("fr_CA")) {
                countryCode = "qc";
            }
            if (localeAsString.equals("es_US")) {
                countryCode = "espanol";
            }


            if (!YAHOO_SEARCH_IN_THE_WORLD.containsKey(countryCode)) {
                countryCode = "US";
            }
            ret.add(new WebSearchEngine(YAHOO_SEARCH_IN_THE_WORLD.get(countryCode).first, YAHOO_SEARCH_IN_THE_WORLD.get(countryCode).second));
            ret.add(new WebSearchEngine("Yahoo (United States)", YAHOO_SEARCH_IN_THE_WORLD.get("US").second));
        }

        if ("google".startsWith(engine)) {
            ret.add(new WebSearchEngine("Google", "https://www.google.com/search?q="));
            ret.add(new WebSearchEngine("Google (English)", "https://www.google.com/search?hl=en&q="));
        }

        return ret;
    }

    public List<WebSearchEngine> getStaticPageListByNameQuery(String query) {
        List<WebSearchEngine> ret = new ArrayList<>();

        for (URLEntry e : this._customStaticURLs) {
            if (e.name.startsWith(query)) {
                ret.add(new WebSearchEngine(e.display_name, e.url_base));
            }
        }

        return ret;
    }
}