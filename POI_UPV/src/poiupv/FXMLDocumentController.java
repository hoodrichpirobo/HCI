package poiupv;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * Controlador unificado para:
 *  - mapa con zoom, POIs y lista
 *  - sección de preguntas tipo test
 *  - login mediante diálogo
 */
public class FXMLDocumentController implements Initializable {

    // === Campos FXML ===
    @FXML private ListView<Poi> map_listview;
    @FXML private ScrollPane map_scrollpane;
    @FXML private Slider zoom_slider;
    @FXML private MenuButton map_pin;
    @FXML private MenuItem pin_info;
    @FXML private SplitPane splitPane;
    @FXML private Label mousePosition;

    // Sección de preguntas
    @FXML private VBox seccionPreguntas;
    @FXML private Text tituloTest;
    @FXML private HBox botonesSeleccionPregunta;
    @FXML private Button seleccionarPregunta;
    @FXML private Button preguntaRandom;
    @FXML private Text enunciadoPregunta;

    // === Estado interno ===
    private Group zoomGroup;
    private ChangeListener<Number> bloqueoDivisor;
    private final BooleanProperty sesionIniciada = new SimpleBooleanProperty(false);

    // === Inicialización ===
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1) Zoom setup
        zoom_slider.setMin(0.5);
        zoom_slider.setMax(1.5);
        zoom_slider.setValue(1.0);
        zoom_slider.valueProperty().addListener((o, oldV, newV) -> applyZoom(newV.doubleValue()));

        // Wrap content in Group for proper zoom scroll recalculation
        Group contentGroup = new Group();
        zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(map_scrollpane.getContent());
        map_scrollpane.setContent(contentGroup);

        // 2) Ocultar sección de preguntas hasta login
        Platform.runLater(() -> {
            seccionPreguntas.setVisible(false);
            tituloTest.setVisible(false);
            botonesSeleccionPregunta.setVisible(false);
            seleccionarPregunta.setVisible(false);
            preguntaRandom.setVisible(false);
            enunciadoPregunta.setVisible(false);
            splitPane.setDividerPositions(0.0);

            // Bloquear divisor en 0 cuando no hay sesión
            bloqueoDivisor = (obs, o, n) -> {
                if (Math.abs(n.doubleValue()) > 1e-4) {
                    splitPane.setDividerPositions(0.0);
                }
            };
            splitPane.getDividers().get(0).positionProperty().addListener(bloqueoDivisor);

            sesionIniciada.addListener((obs, was, isNow) -> {
                if (isNow) {
                    // Desbloquea y muestra preguntas
                    splitPane.getDividers().get(0).positionProperty().removeListener(bloqueoDivisor);
                    splitPane.setDividerPositions(0.35);
                    seccionPreguntas.setVisible(true);
                    tituloTest.setVisible(true);
                    botonesSeleccionPregunta.setVisible(true);
                    seleccionarPregunta.setVisible(true);
                    preguntaRandom.setVisible(true);
                    enunciadoPregunta.setVisible(true);
                } else {
                    // Vuelve a ocultar
                    splitPane.setDividerPositions(0.0);
                    splitPane.getDividers().get(0).positionProperty().addListener(bloqueoDivisor);
                    seccionPreguntas.setVisible(false);
                    tituloTest.setVisible(false);
                    botonesSeleccionPregunta.setVisible(false);
                    seleccionarPregunta.setVisible(false);
                    preguntaRandom.setVisible(false);
                    enunciadoPregunta.setVisible(false);
                }
            });
        });
    }

    // === Zoom handlers ===
    private void applyZoom(double scale) {
        double h = map_scrollpane.getHvalue();
        double v = map_scrollpane.getVvalue();
        zoomGroup.setScaleX(scale);
        zoomGroup.setScaleY(scale);
        map_scrollpane.setHvalue(h);
        map_scrollpane.setVvalue(v);
    }
    @FXML private void zoomIn(ActionEvent e)  { zoom_slider.setValue(zoom_slider.getValue() + 0.1); }
    @FXML private void zoomOut(ActionEvent e) { zoom_slider.setValue(zoom_slider.getValue() - 0.1); }

    // === POI handlers ===
    @FXML
    private void listClicked(MouseEvent e) {
        Poi p = map_listview.getSelectionModel().getSelectedItem();
        if (p == null) return;
        // Animación scroll
        double w = zoomGroup.getBoundsInLocal().getWidth();
        double h = zoomGroup.getBoundsInLocal().getHeight();
        double targetH = p.getPosition().getX() / w;
        double targetV = p.getPosition().getY() / h;
        Timeline tl = new Timeline(
            new KeyFrame(Duration.millis(500),
                new KeyValue(map_scrollpane.hvalueProperty(), targetH),
                new KeyValue(map_scrollpane.vvalueProperty(), targetV)
            )
        );
        tl.play();
        // Posicionar pin
        map_pin.setLayoutX(p.getPosition().getX());
        map_pin.setLayoutY(p.getPosition().getY());
        pin_info.setText(p.getDescription());
        map_pin.setVisible(true);
    }

    @FXML
    private void addPoi(MouseEvent e) {
        if (!e.isControlDown()) return;
        Dialog<Poi> dlg = new Dialog<>();
        dlg.setTitle("Nuevo POI");
        dlg.setHeaderText("Introduce un nuevo POI");
        Stage st = (Stage) dlg.getDialogPane().getScene().getWindow();
        st.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));

        ButtonType ok = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField name = new TextField();
        name.setPromptText("Nombre");
        TextArea desc = new TextArea();
        desc.setPromptText("Descripción");
        desc.setPrefRowCount(4);

        VBox vb = new VBox(10, new Label("Nombre:"), name, new Label("Descripción:"), desc);
        dlg.getDialogPane().setContent(vb);

        dlg.setResultConverter(bt -> bt == ok
            ? new Poi(name.getText().trim(), desc.getText().trim(),
                      0, 0)
            : null
        );
        Optional<Poi> res = dlg.showAndWait();
        res.ifPresent(poi -> {
            Point2D pt = zoomGroup.sceneToLocal(e.getSceneX(), e.getSceneY());
            poi.setPosition(pt);
            map_listview.getItems().add(poi);
        });
    }

    // === Mouse position display ===
    @FXML
    private void showPosition(MouseEvent e) {
        mousePosition.setText(
            "sceneX: " + (int)e.getSceneX() + ", sceneY: " + (int)e.getSceneY()
          + "\nX: " + (int)e.getX() + ", Y: " + (int)e.getY());
    }

    // === About dialog ===
    @FXML
    private void about(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        Stage st = (Stage) a.getDialogPane().getScene().getWindow();
        st.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        a.setTitle("Acerca de");
        a.setHeaderText("IPC - 2025");
        a.showAndWait();
    }

    // === Login dialog ===
    @FXML
    private void onLogin(ActionEvent e) {
        Dialog<Pair<String,String>> dlg = new Dialog<>();
        dlg.setTitle("Iniciar sesión");
        dlg.setHeaderText("Introduce tus credenciales");

        ButtonType loginBtn = new ButtonType("Entrar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        TextField userField = new TextField();
        userField.setPromptText("Usuario");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Contraseña");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Usuario:"),    0, 0);
        grid.add(userField,                1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passField,                1, 1);

        dlg.getDialogPane().setContent(grid);
        Platform.runLater(userField::requestFocus);

        dlg.setResultConverter(bt -> bt == loginBtn
            ? new Pair<>(userField.getText().trim(), passField.getText())
            : null
        );

        dlg.showAndWait().ifPresent(creds -> {
            try {
                Optional<User> opt = new UserDAO().findByUsername(creds.getKey());
                if (opt.isPresent() && opt.get().getPassword().equals(creds.getValue())) {
                    sesionIniciada.set(true);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos")
                        .showAndWait();
                }
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Error BD: " + ex.getMessage())
                    .showAndWait();
            }
        });
    }
}