package no.nav.familie.ks.oppslag.azure.domene;

public class Gruppe {

    private String id;
    private String onPremisesSamAccountName;
    private String displayName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOnPremisesSamAccountName() {
        return onPremisesSamAccountName;
    }

    public void setOnPremisesSamAccountName(String onPremisesSamAccountName) {
        this.onPremisesSamAccountName = onPremisesSamAccountName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
