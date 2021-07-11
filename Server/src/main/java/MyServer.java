import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private static final Logger LOGGER = LogManager.getLogger(MyServer.class);

    private static MyServer server;

    public static MyServer getServer() {
        return server;
    }

    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    public MyServer() {
        server = this;
        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Ждем подключения клиента");
                LOGGER.info("Сервер запущен");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                LOGGER.info("Клиент подключился");
                new ClientHandler(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Ошибка подключения");
        } finally {
            if (authService != null) {
                authService.stop();
                LOGGER.info("Сервис авторизации остановился");
            }
        }
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMsg(msg);
        }
    }

    public synchronized void sendMsgToClient(ClientHandler fromClient, String nickTo, String msg) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nickTo)) {
                client.sendMsg("от пользователя " + fromClient.getName() + ": " + msg);
                fromClient.sendMsg("клиенту " + nickTo + ": " + msg);
                LOGGER.info("сообщение от " + fromClient.getName() + " " + nickTo + ": " + msg);
                return;
            }
        }
        fromClient.sendMsg("Участник с ником \"" + nickTo + "\" не найден");
        LOGGER.info("Ошибка отправки сообщения " + ": Участник с ником \"" + nickTo + "\"не найден!!!\"");
    }

    public synchronized void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clients");
        for (ClientHandler client : clients) {
            sb.append(" ").append(client.getName());
        }
        broadcastMsg(sb.toString());
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public AuthService getAuthService() {
        return authService;
    }
}