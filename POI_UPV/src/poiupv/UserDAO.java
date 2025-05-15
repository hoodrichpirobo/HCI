package poiupv;

import java.sql.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class UserDAO {
    // 1) Construimos la URL en tiempo de ejecución
    private static final String URL;
    static {
        // user.dir = la carpeta raíz desde donde ejecutas (el proyecto o el JAR)
        Path dbPath = Paths.get(System.getProperty("user.dir"), "data", "data.db");
        URL = "jdbc:sqlite:" + dbPath.toString();
    }

    // 2) Conecta FORZANDO la carga del driver
    private Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se cargó el driver de SQLite", e);
        }
        return DriverManager.getConnection(URL);
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void save(User u) throws SQLException {
        String sql = "INSERT INTO users(username,email,password,dob,avatar_path) VALUES(?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getDob());
            ps.setString(5, u.getAvatarPath());
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setDob(rs.getString("dob"));
        u.setAvatarPath(rs.getString("avatar_path"));
        return u;
    }
}