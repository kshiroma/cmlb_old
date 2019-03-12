package jp.co.camnet.cmlb.http;

public class BadRequest extends Exception {
    private String request;

    public BadRequest(String request) {
        this.request = request;
    }

}
