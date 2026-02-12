package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.icc.broadcast.config.MachineConfig;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

@Service
@Slf4j
@RequiredArgsConstructor
public class MachineCommonService {

    private final Environment environment;

    private final MachineConfig machineConfig;

    public String getMachineKey() {
        String hostname = getHostname();
        String ip = getIp();
        String port = getHostPort();

        log.info("host info: {}, {}, {}", hostname, ip, port);

        return sha1(hostname + ":" + ip + ":" + port);
    }

    public String getIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();

                String niName = ni.getName();
                String displayName = ni.getDisplayName();
                log.info("ni name: {}, disp name: {}", niName, displayName);

                if(!niName.equals(machineConfig.getNetworkInterfaceName())) {
                    continue;
                }

                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress inetAddress = addrs.nextElement();
                    String address = inetAddress.getHostAddress();
                    log.info("niName: {}, ip address: {}", niName, address);
                    if(address.startsWith(machineConfig.getAddressPrefix())) {
                        return address;
                    }
                }
            }
        } catch (Exception e) {
            log.error("get ip error", e);
        }

        return "";
    }

    public String getHostPort() {
        return environment.getProperty("local.server.port");
    }

    public String getHostname() {
        String hostname = "Unknown";

        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            log.error("Hostname can not be resolved", ex);
        }

        return hostname;
    }

    public String getHostAddress() {
        String hostAddress = "0.0.0.0";

        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostAddress = addr.getHostAddress();
        } catch (UnknownHostException ex) {
            log.error("Host address can not be resolved", ex);
        }

        return hostAddress;
    }

    public static String sha1(String str) {
        return DigestUtils.sha1Hex(str);
    }
}
