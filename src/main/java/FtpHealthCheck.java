import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import java.net.InetAddress;

/**
 * Checks an FTP server to ensure it is running, and has a minimum number of files.
 * It is assumed that the FTP server is running in an Auto Scaling Group.
 *
 *  Written by George Ludwig, Solutions Architect, Global Alliances at Indeed
 *  June 2018
 */
public class FtpHealthCheck {

    /**
     * The target instance will be set to the Unhealthy state after a single health check failure,
     * causing the autoscaling group to replace it.
     *
     * The arguments are:
     * 0. host - the address the health check, i.e. ftp.domain.com or an IP address
     * 1. port - the port to use for FTP connection i.e. 21
     * 2. user - ftp username
     * 3. password - ftp password
     * 4. timeout - the timeout of the ftp client, will be used for both connect timeout and data timeout; 10000 is a good number
     * 5. min_file_count - the minimum number of files to be present in the ftp server, typically 1
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Region region = Regions.getCurrentRegion();
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String user = args[2];
        String password = args[3];
        int timeout = Integer.parseInt(args[4]);
        int min_file_count = Integer.parseInt(args[5]);
        InetAddress address = InetAddress.getByName(host);
        String ipAddress = address.getHostAddress();
        int fileCount = -1;
        try {
            fileCount = Util.getFtpFileCount(host, port, user, password, timeout);
            System.out.println("host:"+host+" port:"+port+" user:"+user+" pwd:"+password+" timeout:"+timeout+" minFiles:"+min_file_count+" fileCount:"+fileCount);
            if(fileCount < min_file_count)
                Util.autoscaleFail(ipAddress, region.getName());
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("host:"+host+" port:"+port+" user:"+user+" pwd:"+password+" timeout:"+timeout+" minFiles:"+min_file_count+" fileCount:"+fileCount);
            Util.autoscaleFail(ipAddress, region.getName());
        }
    }
}
