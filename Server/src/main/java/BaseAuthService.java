import java.sql.*;

public class BaseAuthService implements AuthService {
    private Connection postgresConnection;
    private static PreparedStatement stmt;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            postgresConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5433/UserList", "postgres", "123");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickByLoginPass(String loginEnter, String password) {
        try {
            PreparedStatement userResSet = postgresConnection.prepareStatement("SELECT * FROM Users where login = ? and pass = ?");
            userResSet.setString(1,loginEnter);
            userResSet.setString(2,password);
            ResultSet nickResSet = userResSet.executeQuery();
            while (nickResSet.next()) {
                return nickResSet.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void stop() {

        System.out.println("Сервис остановился");
    }
}