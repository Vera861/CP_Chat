public interface AuthService {
    void start();

    String getNickByLoginPass(String login, String password);

    void stop();
}