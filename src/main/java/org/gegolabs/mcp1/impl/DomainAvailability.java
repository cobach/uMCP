package org.gegolabs.mcp1.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.whois.WhoisClient;
import org.gegolabs.mcp1.protocol.CapabilityException;
import org.gegolabs.mcp1.protocol.Info;
import org.gegolabs.mcp1.protocol.SyncCapability;

import java.io.IOException;
import java.net.SocketException;

/**
 * A capability that checks if a domain name is available for registration
 * by performing a WHOIS query against the domain registry.
 */
@Slf4j
@Info("Checks if a domain name is available for registration based on a domain registry query.")
public class DomainAvailability implements SyncCapability<String,Boolean> {

    /**
     * Default constructor for DomainAvailability.
     */

    /**
     * Checks if a domain name is available for registration.
     *
     * @param domainName the domain name to check (e.g., 'example.com')
     * @return true if the domain is available, false if it is already registered
     * @throws CapabilityException if there is an error checking the domain availability
     */
    @Override
    @Info("True if the domain is available, false if it is already registered")
    public Boolean execute(@Info("The domain name to check (e.g., 'example.com')") String domainName) throws CapabilityException {
        DomainAvailability obj = new DomainAvailability();
        String result = obj.whois(domainName);
        if (result.startsWith("No match for")) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Performs a WHOIS query for the specified domain name.
     *
     * @param domainName the domain name to query
     * @return the WHOIS query result as a string
     */
    public String whois(String domainName) {
        StringBuilder whoisResult = new StringBuilder();
        WhoisClient crunchifyWhois = new WhoisClient();
        try {
            // The WhoisClient class implements the client side of the Internet
            // Whois Protocol defined in RFC 954. To query a host you create a
            // WhoisClient instance, connect to the host, query the host, and
            // finally disconnect from the host. If the whois service you want
            // to query is on a non-standard port, connect to the host at that
            // port.
            crunchifyWhois.connect(WhoisClient.DEFAULT_HOST);
            String whoisData = crunchifyWhois.query("=" + domainName);
            // append(): Appends the specified string to this character sequence.
            // The characters of the String argument are appended, in order, increasing
            // the length of this sequence by the length of the argument.
            // If str is null, then the four characters "null" are appended.
            whoisResult.append(whoisData);
            crunchifyWhois.disconnect();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return whoisResult.toString();
    }
}
