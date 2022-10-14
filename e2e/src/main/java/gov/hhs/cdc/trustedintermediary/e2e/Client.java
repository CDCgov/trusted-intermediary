package gov.hhs.cdc.trustedintermediary.e2e;

public class Client {
    public static String callService(String path) {
        System.out.println("Calling the backend at " + path);
        return "DogCow";
    }
}
