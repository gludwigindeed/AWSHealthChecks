import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/**
 * Pings the Health Check URL of an HTTP server.
 * It is assumed that the HTTP server is running in an Auto Scaling Group.
 *
 *  Written by George Ludwig, Solutions Architect, Global Alliances at Indeed
 *  June 2018
 */
public class HttpHealthCheck {

    /**
     * This is a health check for a server in an auto-scaling group that has an HTTP-based
     * Health Check endpoint.
     *
     * Any response code other than 200 is interpreted to mean that the health check has failed
     * Also, a timeout or Exception is interpreted as a health check fail.
     *
     * The target instance will be set to the Unhealthy state after a single health check failure,
     * causing the autoscaling group to replace it.
     *
     * Two arguments are required:
     * 1. url - the full URL of the health check, i.e. http://my.domain.com/HealthCheck
     * 2. read_timeout - the read timeout of the healthcheck
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String ipAddress = null;
        Region region = Regions.getCurrentRegion();
        URL url = new URL(args[0]);
        String host = url.getHost();
        InetAddress address = InetAddress.getByName(host);
        ipAddress = address.getHostAddress();
        int timeout = Integer.parseInt(args[1]);
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(timeout);
            con.getContent();
            // get response code...any code other than 200 io a fail
            int code = con.getResponseCode();
            if(code!=200)
                Util.autoscaleFail(ipAddress, region.getName());
        } catch(Exception e) {
            e.printStackTrace();
            Util.autoscaleFail(ipAddress, region.getName());
        }
    }
}
