package org.gegolabs.mcp1.impl;

import org.gegolabs.mcp1.protocol.CapabilityException;
import org.gegolabs.mcp1.protocol.Info;
import org.gegolabs.mcp1.protocol.SyncCapability;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * A capability that provides detailed information about the system environment.
 * This includes OS details, Java version, memory usage, and other system properties.
 */
@Info("Provides detailed information about the system environment")
public class SystemInformation implements SyncCapability<Void,String>{

    /**
     * Default constructor for SystemInformation.
     */

    /**
     * Executes the system information capability.
     *
     * @param input no input required (Void)
     * @return a formatted string containing system information including OS details, Java version, memory usage, etc.
     * @throws CapabilityException if there is an error retrieving system information
     */
    @Override
    @Info("A formatted string containing system information including OS details, Java version, memory usage, etc.")
    public String execute(@Info("No input required (Void)") Void input) throws CapabilityException {
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
