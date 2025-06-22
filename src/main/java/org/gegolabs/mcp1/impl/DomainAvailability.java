package org.gegolabs.mcp1.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.whois.WhoisClient;
import org.gegolabs.mcp1.protocol.CapabilityException;
import org.gegolabs.mcp1.protocol.Description;
import org.gegolabs.mcp1.protocol.Name;
import org.gegolabs.mcp1.protocol.SyncCapability;

import java.io.IOException;
import java.net.SocketException;

/**
 * A capability that checks if a domain name is available for registration
 * by performing a WHOIS query against the domain registry.
 */
@Slf4j
@Description("Checks if a domain name is available for registration based on a domain registry query.")
@Name("domain-availability")
public class DomainAvailability implements SyncCapability<String,Boolean> {

    /**
     * The WhoisClient instance used for WHOIS queries.
     */
    private WhoisClient whoisClient;

    /**
     * Default constructor for DomainAvailability.
     */
    public DomainAvailability() {
        // Client will be initialized in initialize() method
    }

    /**
     * Initializes the capability by creating a WhoisClient instance.
     *
     * @throws CapabilityException if initialization fails
     */
    @Override
    public void initialize() throws CapabilityException {
        log.info("Initializing DomainAvailability capability");
        try {
            whoisClient = new WhoisClient();
        } catch (Exception e) {
            throw new CapabilityException("Failed to initialize WhoisClient", e);
        }
    }

    /**
     * Shuts down the capability by disconnecting the WhoisClient if it's connected.
     *
     * @throws CapabilityException if shutdown fails
     */
    @Override
    public void shutdown() throws CapabilityException {
        log.info("Shutting down DomainAvailability capability");
        if (whoisClient != null && whoisClient.isConnected()) {
            try {
                whoisClient.disconnect();
            } catch (IOException e) {
                throw new CapabilityException("Failed to disconnect WhoisClient", e);
            }
        }
    }

    /**
     * Checks if a domain name is available for registration.
     *
     * @param domainName the domain name to check (e.g., 'example.com')
     * @return true if the domain is available, false if it is already registered
     * @throws CapabilityException if there is an error checking the domain availability
     */
    @Override
    @Description("True if the domain is available, false if it is already registered")
    public Boolean execute(@Description("The domain name to check (e.g., 'example.com')") String domainName) throws CapabilityException {
        String result = whois(domainName);
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
     * @throws CapabilityException if there is an error performing the WHOIS query
     */
    public String whois(String domainName) throws CapabilityException {
        StringBuilder whoisResult = new StringBuilder();
        try {
            // Connect to the WHOIS server if not already connected
            if (!whoisClient.isConnected()) {
                whoisClient.connect(WhoisClient.DEFAULT_HOST);
            }

            // Query the WHOIS server
            String whoisData = whoisClient.query("=" + domainName);
            whoisResult.append(whoisData);

            // Note: We don't disconnect after each query to allow reuse of the connection
        } catch (SocketException e) {
            log.error("Socket error during WHOIS query", e);
            throw new CapabilityException("Socket error during WHOIS query", e);
        } catch (IOException e) {
            log.error("I/O error during WHOIS query", e);
            throw new CapabilityException("I/O error during WHOIS query", e);
        }
        return whoisResult.toString();
    }
}
