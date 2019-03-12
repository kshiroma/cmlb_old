package jp.co.camnet.cmlb;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JettyHandler extends AbstractHandler {
    @Override
    public void handle(
            String s,
            Request request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) throws IOException, ServletException {
        if (request.getMethod().equals(HttpMethod.GET.asString())) {
            httpServletResponse.getWriter().write(httpServletRequest.getRequestURI());
            httpServletResponse.getWriter().write("\n");
            httpServletResponse.getWriter().write(httpServletRequest.getRequestURL().toString());
            httpServletResponse.getWriter().write("\n");
            httpServletResponse.getWriter().write(String.valueOf(httpServletRequest.getLocalPort()));
            httpServletResponse.getWriter().write("\n");
            System.out.println("GET");
        }
        if (request.getMethod().equals(HttpMethod.POST.asString())) {
            //System.out.println("POST");
            request.getResponse().getWriter().write("POST\n");
            byte[] message = new byte[4096];
            int size = request.getInputStream().read(message);
            while (size > 0) {
                //request.getResponse().getWriter().write(message);
                for (int i = 0; i < size; i++) {
                    request.getResponse().getWriter().write(message[i]);
                }
                size = request.getInputStream().read(message);
            }
            System.out.write('\n');
            System.out.flush();
        }
        if (request.getMethod().equals(HttpMethod.PUT.asString())) {
            System.out.println("PUT");
            int message = request.getInputStream().read();
            while (message != -1) {
                System.out.write(message);
                message = request.getInputStream().read();
            }
        }
        request.setHandled(true);
        httpServletResponse.getWriter().flush();
        httpServletResponse.getWriter().close();
    }
}
