package jp.co.camnet.cmlb.http;

public class HttpRequestFirstLine {
    private String method;
    private String uri;
    private String protocolVersion;
    private String request;


    public HttpRequestFirstLine(String request) throws BadRequest {
        this.request = request;
        try {
            String[] array = request.split("\\s+");
            method = array[0];
            uri = array[1];
            protocolVersion = array[2];
        } catch (Exception e) {
            throw new BadRequest(request);
        }
    }


    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }


    public String getRequest() {
        return request;
    }

}
