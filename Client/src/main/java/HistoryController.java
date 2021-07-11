import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HistoryController{

    private File fileLog;

    public HistoryController(String login) {
        this.fileLog = new File("history_" + login + ".txt");
        if (!fileLog.exists()) {
            try {
                fileLog.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void saveMsg(String msg) {
        try (FileWriter fileWriter = new FileWriter(fileLog, true)) {
            fileWriter.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
