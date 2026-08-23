package com.money.manager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIf(value = "com.money.manager.ManagerApplicationTests#fullContextInfraAvailable",
        disabledReason = "Requires PostgreSQL on localhost:5432 and the JWT_KEY env var")
class ManagerApplicationTests {

	private static final int POSTGRES_PORT = 5432;

	@Test
	void contextLoads() {
	}

	static boolean fullContextInfraAvailable() {
		return postgresReachable() && jwtKeyConfigured();
	}

	private static boolean postgresReachable() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("localhost", POSTGRES_PORT), 500);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	private static boolean jwtKeyConfigured() {
		String jwtKey = System.getenv("JWT_KEY");
		return jwtKey != null && !jwtKey.isBlank();
	}

}
