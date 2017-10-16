package rocks.massi.trollsgames.data;

public enum ThirdPartyServices {
    PHILIBERT("Philibert"),
    TRICTRAC("TricTrac");

    private String serviceName;

    private ThirdPartyServices(String serviceName) {
        this.serviceName = serviceName;
    }
}
