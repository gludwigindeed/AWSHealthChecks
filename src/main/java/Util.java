import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.SetInstanceHealthRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.util.Arrays;
import java.util.List;

/**
 * Util class for health checks
 * Written by George Ludwig, Solutions Architect, Global Alliances at Indeed
 * June 2018
 */
public class Util {

    /**

     * For an EC2 instance that is in an Autoscaling Group, that has the IP address,
     * and is in the given AWS region, set the instance state ot "Unhealthy".
     *
     * @param ipAddress
     * @param region
     */
    public static void autoscaleFail(String ipAddress, String region) {
        System.out.println("failing "+ipAddress+" "+region);
        // get ec2 client
        AmazonEC2 ec2Client = AmazonEC2ClientBuilder.standard().withRegion(region).build();
        // get the instance id of target machine
        List<String> ipList = Arrays.asList(ipAddress);
        DescribeInstancesRequest ec2Request = new DescribeInstancesRequest()
                .withFilters(new Filter("ip-address",ipList));
        DescribeInstancesResult reservations = ec2Client.describeInstances(ec2Request);
        // should only be one instance with the designated IP address
        List<Reservation> reservationList=reservations.getReservations();
        Reservation target = reservationList.get(0);
        List<Instance>instances = target.getInstances();
        // get autoscaling client
        AmazonAutoScaling asClient = AmazonAutoScalingClientBuilder.standard().withRegion(region).build();
        // create health request
        String instanceId = instances.get(0).getInstanceId();
        SetInstanceHealthRequest healthReq = new SetInstanceHealthRequest()
                .withHealthStatus("Unhealthy")
                .withShouldRespectGracePeriod(Boolean.FALSE)
                .withInstanceId(instanceId);
        // set instance unhealthy
        asClient.setInstanceHealth(healthReq);
    }

    /**
     * Get a count of the files at the given FTP location.
     *
     * @param host the FTP host address
     * @param port the FTP connection port
     * @param username the FTP username
     * @param password the password for the FTP username
     * @param timeout the connect AND data timeout, in ms
     * @return int representing the number of files found
     * @throws Exception
     */
    public static int getFtpFileCount(String host, int port, String username, String password, int timeout) throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(timeout);
        ftpClient.setDataTimeout(timeout);
        ftpClient.connect(host, port);
        ftpClient.enterLocalPassiveMode();
        ftpClient.login(username, password);
        FTPFile[] files = ftpClient.listFiles();
        return files.length;
    }
}


