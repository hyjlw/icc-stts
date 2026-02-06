package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MachineCommonService {

    private final Environment environment;

    public String getMachineKey() {
        String port = environment.getProperty("local.server.port");
        String hostname = getHostname();
        String ip = getHostAddress();

        return sha1(hostname + ":" + ip + ":" + port);
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
            System.err.println("Hostname can not be resolved");
        }

        return hostname;
    }

    public String getHostAddress() {
        String hostAddress = "0.0.0.0";

        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostAddress = addr.getHostAddress();
        } catch (UnknownHostException ex) {
            System.err.println("Host address can not be resolved");
        }

        return hostAddress;
    }

    public static String sha1(String str) {
        return DigestUtils.sha1Hex(str);
    }
}
