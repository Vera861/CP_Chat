import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler {
    private MyServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name = "";
    private final String root = "server/serverFiles";
    private byte[] buffer;


    public ClientHandler(Socket socket) {
        try {
            this.server = MyServer.getServer();
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                     auth();
                    readFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Проблемы при создании обработчика клиента");
            e.printStackTrace();
        }
    }

    private void auth() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split(" ");
                String login = parts[1];
                String password = parts[2];
                String nick = server.getAuthService().getNickByLoginPass(login, password);
                if (nick != null) {
                    if (!server.isNickBusy(nick)) {
                        sendMsg("/authok " + nick);
                        name = nick;
                        server.broadcastMsg(name + " авторизовался");
                        server.subscribe(this);
                        return;
                    } else {
                        sendMsg("Учетная запись уже используется");
                    }
                } else {
                    if (!server.isNickBusy(nick)) {
                        sendMsg("/authok " + nick);
                        name = "Инкогнито";
                        server.broadcastMsg(name + " зашел в чат");
                        server.subscribe(this);
                        return;
                    } else {
                        sendMsg("Учетная запись уже используется");
                    }
                    sendMsg("Неверные логин/пароль");
                }
            } else {
                sendMsg("Перед тем как отправлять файлы авторизуйтесь через команду </auth login1 pass1>");
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile() throws IOException {
        while (socket.isConnected()) {
            String fileName = in.readUTF();
            System.out.println("Received fileName " + fileName);
            Path file = Paths.get(root, fileName);
            long fileSize = in.readLong();
            System.out.println("Received fileSize" + fileSize);
            try (FileOutputStream fos = new FileOutputStream(root + "/" + fileName)) {
                for (int i = 0; i < (fileSize + 255) / 256; i++) {
                    int read = in.read(buffer);
                    fos.write(buffer, 0, read);
                }
                fos.flush();
            }
            out.writeUTF("File " + fileName + " loaded");
                }
    }

    public void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {

        return name;
    }
}