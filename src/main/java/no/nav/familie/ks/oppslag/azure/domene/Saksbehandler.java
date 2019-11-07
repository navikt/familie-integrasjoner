package no.nav.familie.ks.oppslag.azure.domene;

import java.util.LinkedList;
import java.util.List;

public class Saksbehandler {
    private String userPrincipalName;
    private String onPremisesSamAccountName; //Navident
    private List<Gruppe> grupper = new LinkedList<>();

    public Saksbehandler() {}

    public Saksbehandler(String userPrincipalName, String onPremisesSamAccountName) {
        this.userPrincipalName = userPrincipalName;
        this.onPremisesSamAccountName = onPremisesSamAccountName;
    }

    public String getOnPremisesSamAccountName() {
        return onPremisesSamAccountName;
    }

    public List<Gruppe> getGrupper() {
        return grupper;
    }

    public void setGrupper(List<Gruppe> grupper) {
        this.grupper = grupper;
    }

    @Override
    public String toString() {
        return "Saksbehandler{" +
               "userPrincipalName='" + userPrincipalName + '\'' +
               ", onPremisesSamAccountName='" + onPremisesSamAccountName + '\'' +
               ", grupper=" + grupper +
               '}';
    }
}
