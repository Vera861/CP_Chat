import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatController {
    private final String root = "client/clientFiles";
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private byte[] buffer;

    public ListView<String> listView;
    public TextField statusBar;

    @FXML
    private void initialize() throws IOException {
        try {
            openLoginWindow();
            Main.mainStage.setTitle(Main.mainStage.getTitle());
            File dir = new File(root);
            listView.getItems().clear();
            listView.getItems().addAll(dir.list());

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String status = in.readUTF();
                        Platform.runLater(() -> statusBar.setText(status)
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
            openConnection();
            addCloseListener();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка подключения");
            alert.setHeaderText("Сервер не работает");
            alert.setContentText("Необходимо включить сервер!");
            alert.showAndWait();
            e.printStackTrace();
            throw e;
        }
    }

    private void openLoginWindow() throws IOException {
        Parent root = FXMLLoader.load(ClassLoader.getSystemResource("auth.fxml"));
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setScene(new Scene(root));
        loginStage.setTitle("Авторизация");
        loginStage.showAndWait();
    }

    private void openConnection() throws IOException {
        socket = ServerConnection.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
//        HistoryController fileLog = new HistoryController(this.login);
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String strFromServer = in.readUTF();
                    if (strFromServer.equalsIgnoreCase("/end")) {
                        break;
                    }
//                    fileLog.saveMsg(strFromServer + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void addCloseListener() {
        EventHandler<WindowEvent> onCloseRequest = Main.mainStage.getOnCloseRequest();
        Main.mainStage.setOnCloseRequest(event -> {
            closeConnection();
            if (onCloseRequest != null) {
                onCloseRequest.handle(event);
            }
        });
    }

    private void closeConnection() {
        try {
            out.writeUTF("/end");
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendFile(){

        try {
            String fileName = listView.getSelectionModel().getSelectedItem();
            Path filePath = Paths.get(root, fileName);
            long fileSize = Files.size(filePath);
            out.writeUTF(fileName);
            out.writeLong(fileSize);
            Files.copy(filePath, out);
            out.flush();
            statusBar.setText("File: " + fileName + " sended");

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка отправки сообщения");
            alert.setHeaderText("Ошибка отправки сообщения");
            alert.setContentText("При отправке сообщения возникла ошибка: " + e.getMessage());
            alert.show();
        }
    }
}