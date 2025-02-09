package com.capstone.cloudcontrol.remotepccontrol.dashcontrols;

import org.springframework.web.bind.annotation.*;
import java.io.*;

@RestController
@RequestMapping("/api")
public class DashController {

    private static final String DASH_CLI_DIR = "C:\\Program Files\\DASH CLI 7.0\\bin";

    @GetMapping("/check_status")
    public String checkStatus(@RequestParam String host, @RequestParam String user, @RequestParam String password) {
        return executeDashCommand(host, user, password, "status");
    }

    @PostMapping("/start_kvm")
    public String startKvm(@RequestParam String host, @RequestParam String user, @RequestParam String password) {
        return executeDashCommand(host, user, password, "kvmredirection[0] startkvm");
    }

    private String executeDashCommand(String host, String user, String password, String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c", ".\\dashcli.exe -h " + host + " -u " + user + " -P " + password + " -S https -C -p 664 -t " + command
            );
            processBuilder.directory(new File(DASH_CLI_DIR));
            processBuilder.redirectErrorStream(true); // Merge output and error streams

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            return exitCode == 0 ? "Command executed successfully:\n" + output.toString()
                                 : "Command execution failed with exit code " + exitCode + "\nOutput:\n" + output.toString();
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }
}
