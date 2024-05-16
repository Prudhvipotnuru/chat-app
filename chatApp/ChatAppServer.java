package chatApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatAppServer {
	private static final int PORT = 9875;
	private static List<Socket> clients = new ArrayList<>();

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server started. Waiting for clients...");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("New client connected: " + clientSocket);

				clients.add(clientSocket);
				// Thread clientThread = new Thread(new ClientHandler(clientSocket));
				// clientThread.start();
				ExecutorService ex = Executors.newFixedThreadPool(10);
				ex.execute(new ClientHandler(clientSocket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ClientHandler implements Runnable {
		private Socket clientSocket;

		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					System.out.println("Message from client: " + inputLine);
					broadcast(inputLine);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void broadcast(String message) {
			for (Socket client : clients) {
				if (client != clientSocket) {
					try {
						PrintWriter out = new PrintWriter(client.getOutputStream(), true);
						out.println(message);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}