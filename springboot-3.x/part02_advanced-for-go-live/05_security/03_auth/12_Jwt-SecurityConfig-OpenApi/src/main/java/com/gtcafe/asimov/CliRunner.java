package com.gtcafe.asimov;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CliRunner implements ApplicationRunner {

    private final JwtUtil jwtUtil;

    @Autowired
    public CliRunner(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("genkey")) {
            List<String> values = args.getOptionValues("genkey");

            if (values == null || values.isEmpty()) {
                System.err.println("Missing value for --genkey (expected format: user,ROLE)");
                System.exit(1);
            }

            String value = values.get(0); // "admin,ADMIN"
            String[] parts = value.split(",");
            if (parts.length != 2) {
                System.err.println("Invalid format: use --genkey=user,ROLE");
                System.exit(1);
            }

            String subject = parts[0];
            String role = parts[1];

            String token = jwtUtil.generateToken(subject, role);
            System.out.println("✅ Generated JWT:");
            System.out.println(token);
            System.exit(0);
        }
    }

}
