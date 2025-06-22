package org.gegolabs.mcp1.impl;

import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp1.protocol.CapabilityException;
import org.gegolabs.mcp1.protocol.Description;
import org.gegolabs.mcp1.protocol.SyncCapability;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * A capability that provides detailed information about the system environment.
 * This includes OS details, Java version, memory usage, and other system properties.
 */
@Slf4j
@Description("Provides detailed information about the system environment")
public class SystemInformation implements SyncCapability<Void,String>{

    /**
     * Default constructor for SystemInformation.
     */
    public SystemInformation() {
        // No initialization needed in constructor
    }

    /**
     * Initializes the capability.
     * This implementation logs the initialization but doesn't need to do any setup.
     *
     * @throws CapabilityException if initialization fails
     */
    @Override
    public void initialize() throws CapabilityException {
        log.info("Initializing SystemInformation capability");
        // No specific initialization needed for this capability
    }

    /**
     * Shuts down the capability.
     * This implementation logs the shutdown but doesn't need to do any cleanup.
     *
     * @throws CapabilityException if shutdown fails
     */
    @Override
    public void shutdown() throws CapabilityException {
        log.info("Shutting down SystemInformation capability");
        // No specific cleanup needed for this capability
    }

    /**
     * Executes the system information capability.
     *
     * @param input no input required (Void)
     * @return a formatted string containing system information including OS details, Java version, memory usage, etc.
     * @throws CapabilityException if there is an error retrieving system information
     */
    @Override
    @Description("A formatted string containing system information including OS details, Java version, memory usage, etc.")
    public String execute(@Description("No input required (Void)") Void input) throws CapabilityException {
        return getSystemReport();
    }

    /**
     * Generates a detailed report of the system information.
     *
     * @return a formatted string containing system information
     */
    public static String getSystemReport() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        StringBuilder report = new StringBuilder();

        report.append("System Information Report\n")
                .append("======================\n\n")
                .append(formatProperty("OS Name", System.getProperty("os.name")))
                .append(formatProperty("OS Version", System.getProperty("os.version")))
                .append(formatProperty("OS Architecture", System.getProperty("os.arch")))
                .append(formatProperty("Available Processors", Runtime.getRuntime().availableProcessors()))
                .append(formatProperty("Java Version", System.getProperty("java.version")))
                .append(formatProperty("Java Home", System.getProperty("java.home")))
                .append(formatProperty("User Name", System.getProperty("user.name")))
                .append(formatProperty("User Home", System.getProperty("user.home")))
                .append(formatProperty("Total Memory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB"))
                .append(formatProperty("Free Memory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + " MB"));

        return report.toString();
    }

    /**
     * Formats a property name and value as a string.
     *
     * @param name the name of the property
     * @param value the value of the property
     * @return a formatted string with the property name and value
     */
    private static String formatProperty(String name, Object value) {
        return String.format("%-20s: %s%n", name, value);
    }
}
