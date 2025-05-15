package poiupv;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String dob;          // “YYYY-MM-DD”
    private String avatarPath;   // nombre o ruta de la imagen

    public User() { }

    // constructor opcional, si quieres usarlo
    public User(String username, String email, String password, String dob, String avatarPath) {
        this.username   = username;
        this.email      = email;
        this.password   = password;
        this.dob        = dob;
        this.avatarPath = avatarPath;
    }

    // getters & setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getDob() {
        return dob;
    }
    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAvatarPath() {
        return avatarPath;
    }
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}