package chatApp;

import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ChatAppClient extends Frame {
	private static final String SERVER_ADDRESS = "localhost";
	private static final int PORT = 9875;

	private TextArea receiveBox;
	private TextField sendBox;
	private PrintWriter out;
	private String username;
	private ArrayList<String> usersList;
	private TextArea usersBox;

	public ChatAppClient(String username) {
		this.username = username;
		setTitle("Person - " + username);
		setSize(600, 500);
		setLayout(null);

		initializeReceiveBox();
		initializeSendBox();
		initializeSendButton();
		initializeUsersBox();
		initializeWindowListener();

		usersList = new ArrayList<>();

		connectToServer();
	}

	private void initializeReceiveBox() {
		receiveBox = new TextArea();
		receiveBox.setEditable(false);
		receiveBox.setBounds(10, 35, 480, 350);
		receiveBox.setFont(new Font("Courier New", Font.PLAIN, 18));
		add(receiveBox);
	}

	private void initializeSendBox() {
		sendBox = new TextField();
		sendBox.setBounds(10, 400, 480, 50);
		add(sendBox);
	}

	private void initializeSendButton() {
		Button send = new Button("Send");
		send.setBounds(500, 400, 80, 50);
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendText();
			}
		});
		add(send);
	}

	private void initializeUsersBox() {
		usersBox = new TextArea();
		usersBox.setEditable(false);
		usersBox.setBounds(500, 35, 80, 350);
		add(usersBox);
	}

	private void initializeWindowListener() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	private void connectToServer() {
		try {
			Socket socket = new Socket(SERVER_ADDRESS, PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			startReceiveThread(in);

			announceUsernameToServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startReceiveThread(BufferedReader in) {
		Thread receiveThread = new Thread(() -> {
			try {
				String message;
				while ((message = in.readLine()) != null) {
					processMessage(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		receiveThread.start();
	}

	private void processMessage(String message) {
		if (message.startsWith("[USERNAME]")) {
			String newUser = message.substring(10);
			usersList.add(newUser);
			updateUserList();
		} else {
			receiveMessage(message);
		}
	}

	private void announceUsernameToServer() {
		out.println("[USERNAME]" + username);
	}

	private void sendText() {
		String message = sendBox.getText();
		if (!message.isEmpty()) {
			out.println(username + ": " + message);
			sendBox.setText("");
			receiveBox.append(username + ": " + message + "\n");
		}
	}

	private void receiveMessage(String message) {
		receiveBox.append(message + "\n");
	}

	private void updateUserList() {
		usersBox.setText("");
		for (String user : usersList) {
			usersBox.append(user + "\n");
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java ChatClient <username>");
			System.exit(1);
		}

		new ChatAppClient(args[0]);
	}
}
