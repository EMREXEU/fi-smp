package fi.csc.emrex.smp;

/**
 * Created by marko.hollanti on 02/09/15.
 */
public class NCPResult {

    private final String countryCode;
    private final String acronym;
    private final String url;

    public NCPResult(String countryCode, String acronym, String url) {
        this.countryCode = countryCode;
        this.acronym = acronym;
        this.url = url;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getUrl() {
        return url;
    }
}
