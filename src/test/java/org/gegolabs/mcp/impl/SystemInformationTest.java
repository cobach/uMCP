package org.gegolabs.mcp.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemInformationTest {

    /**
     * The SystemInformationTest class contains tests for the getSystemReport method within the
     * SystemInformation class. The getSystemReport method generates a detailed report about
     * the system's properties, including OS information, Java details, user details, and memory stats.
     */

    @Test
    void testGetSystemReportContainsOSName() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("OS Name"), "Report should contain 'OS Name' information.");
    }

    @Test
    void testGetSystemReportContainsOSVersion() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("OS Version"), "Report should contain 'OS Version' information.");
    }

    @Test
    void testGetSystemReportContainsOSArchitecture() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("OS Architecture"), "Report should contain 'OS Architecture' information.");
    }

    @Test
    void testGetSystemReportContainsAvailableProcessors() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("Available Processors"), "Report should contain 'Available Processors' information.");
    }

    @Test
    void testGetSystemReportContainsJavaVersion() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("Java Version"), "Report should contain 'Java Version' information.");
    }

    @Test
    void testGetSystemReportContainsJavaHome() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("Java Home"), "Report should contain 'Java Home' information.");
    }

    @Test
    void testGetSystemReportContainsUserName() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("User Name"), "Report should contain 'User Name' information.");
    }

    @Test
    void testGetSystemReportContainsUserHome() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("User Home"), "Report should contain 'User Home' information.");
    }

    @Test
    void testGetSystemReportContainsTotalMemory() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("Total Memory"), "Report should contain 'Total Memory' information.");
    }

    @Test
    void testGetSystemReportContainsFreeMemory() {
        // Act
        String report = SystemInformation.getSystemReport();

        // Assert
        assertTrue(report.contains("Free Memory"), "Report should contain 'Free Memory' information.");
    }
}