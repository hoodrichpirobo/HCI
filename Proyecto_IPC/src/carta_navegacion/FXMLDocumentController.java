package carta_navegacion;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import model.NavDAOException;
import model.Navigation;
import model.Problem;
import carta_navegacion.FXMLDisplayProblemsController;
import model.Answer;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.time.Period;
import model.User;


/**
 * Controlador para:
 * - Mapa con zoom y puntos de interés (POI)
 * - Sección de preguntas tipo test
 * - Login mediante diálogo
 */
public class FXMLDocumentController implements Initializable {

    Navigation obj;
    // === Campos FXML ===
    private ListView<Poi> map_listview;
    @FXML private ScrollPane map_scrollpane;
    @FXML private Slider zoom_slider;
    @FXML private MenuButton map_pin;
    @FXML private MenuItem pin_info;
    @FXML private SplitPane splitPane;
    @FXML private Label mousePosition;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // Sección de preguntas
    @FXML private VBox seccionPreguntas;
    @FXML private Text tituloTest;
    @FXML private HBox botonesSeleccionPregunta;
    @FXML private Button seleccionarPregunta;
    @FXML private Button preguntaRandom;
    @FXML private Text enunciadoPregunta;

    // === Estado interno ===
    private Group zoomGroup;
    private final BooleanProperty sesionIniciada = new SimpleBooleanProperty(false);
    private ChangeListener<Number> bloqueoDivisor;
    @FXML
    private Button centerButton;
    @FXML
    private ToggleButton puntito;
    @FXML
    private ToggleButton transButton;
    @FXML
    private RadioButton ans1;
    @FXML
    private RadioButton ans2;
    @FXML
    private RadioButton ans3;
    @FXML
    private RadioButton ans4;

  
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración del zoom
        zoom_slider.setMin(0.015);
        zoom_slider.setMax(1.5);
        zoom_slider.setValue(0.5);
        zoom_slider.valueProperty().addListener((obs, oldVal, newVal) -> applyZoom(newVal.doubleValue()));

        // Inicialización del zoom
        Group contentGroup = new Group();
        zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(map_scrollpane.getContent());
        map_scrollpane.setContent(contentGroup);
        configurarContenidoMapa();
        // Ocultar sección de preguntas al inicio
        configurarSeccionPreguntas();
        
        
        // Disable the auth buttons once we're logged in
        loginButton.disableProperty().bind(sesionIniciada);
        registerButton.disableProperty().bind(sesionIniciada);

    }
    private void configurarContenidoMapa() {
    // Obtener el contenido original (asumo que es un ImageView)
    Node contenidoOriginal = map_scrollpane.getContent();
    
    // Crear un contenedor que centre el contenido
    StackPane centeringPane = new StackPane();
    centeringPane.getChildren().add(contenidoOriginal);
    centeringPane.setAlignment(Pos.CENTER);
    
    // Configurar el Group para el zoom
    zoomGroup = new Group(centeringPane);
    
    // Crear el contenedor principal
    StackPane contentPane = new StackPane();
    contentPane.getChildren().add(zoomGroup);
    
    // Configurar el ScrollPane
    map_scrollpane.setContent(contentPane);
    map_scrollpane.setFitToWidth(true);
    map_scrollpane.setFitToHeight(true);
    map_scrollpane.setPannable(true);
    
    // Aplicar zoom mínimo inicial
    Platform.runLater(() -> {
        zoomGroup.setScaleX(0.1);
        zoomGroup.setScaleY(0.1);
        centrarContenido();
    });
}
    private void centrarContenido() {
    // Centrar el contenido en el ScrollPane
    map_scrollpane.setHvalue(0.5);
    map_scrollpane.setVvalue(0.5);
}
    private void configurarSeccionPreguntas() {
        Platform.runLater(() -> {
            seccionPreguntas.setVisible(false);
            splitPane.setDividerPositions(0.0);

            bloqueoDivisor = (obs, oldVal, newVal) -> {
                if (Math.abs(newVal.doubleValue()) > 1e-4) {
                    splitPane.setDividerPositions(0.0);
                }
            };
            splitPane.getDividers().get(0).positionProperty().addListener(bloqueoDivisor);

            sesionIniciada.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    splitPane.getDividers().get(0).positionProperty().removeListener(bloqueoDivisor);
                    splitPane.setDividerPositions(0.35);
                    seccionPreguntas.setVisible(true);
                } else {
                    splitPane.setDividerPositions(0.0);
                    splitPane.getDividers().get(0).positionProperty().addListener(bloqueoDivisor);
                    seccionPreguntas.setVisible(false);
                }
            });
        });
    }

    // === Zoom y control del mapa ===

    private void applyZoom(double scale) {
    scale = Math.max(0.1, Math.min(scale, 10.0));

    Node content = map_scrollpane.getContent();
    
    // Obtener las coordenadas del puntero relativas al contenido antes del zoom
    double mouseX = map_scrollpane.getWidth() / 2;
    double mouseY = map_scrollpane.getHeight() / 2;

    Point2D scrollOffset = figureScrollOffset(map_scrollpane, zoomGroup);

    // Aplicar el nuevo zoom
    zoomGroup.setScaleX(scale);
    zoomGroup.setScaleY(scale);

    // Ajustar los valores de scroll para mantener la posición
    repositionScroller(map_scrollpane, zoomGroup, scrollOffset, scale);
}
private Point2D figureScrollOffset(ScrollPane scrollPane, Node content) {
    double extraWidth = content.getBoundsInLocal().getWidth() - scrollPane.getViewportBounds().getWidth();
    double hScrollProportion = scrollPane.getHvalue();
    double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);

    double extraHeight = content.getBoundsInLocal().getHeight() - scrollPane.getViewportBounds().getHeight();
    double vScrollProportion = scrollPane.getVvalue();
    double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);

    return new Point2D(scrollXOffset, scrollYOffset);
}

private void repositionScroller(ScrollPane scrollPane, Node content, Point2D scrollOffset, double scale) {
    double extraWidth = content.getBoundsInLocal().getWidth() - scrollPane.getViewportBounds().getWidth();
    if (extraWidth > 0) {
        scrollPane.setHvalue(scrollOffset.getX() / extraWidth);
    } else {
        scrollPane.setHvalue(0);
    }

    double extraHeight = content.getBoundsInLocal().getHeight() - scrollPane.getViewportBounds().getHeight();
    if (extraHeight > 0) {
        scrollPane.setVvalue(scrollOffset.getY() / extraHeight);
    } else {
        scrollPane.setVvalue(0);
    }
}


    @FXML
    private void zoomIn(ActionEvent event) {
        zoom_slider.setValue(zoom_slider.getValue() + 0.05);
    }

    @FXML
    private void zoomOut(ActionEvent event) {
        zoom_slider.setValue(zoom_slider.getValue() - 0.05);
    }

    private void listClicked(MouseEvent event) {
        Poi selectedPoi = map_listview.getSelectionModel().getSelectedItem();
        if (selectedPoi == null) return;

        double width = zoomGroup.getBoundsInLocal().getWidth();
        double height = zoomGroup.getBoundsInLocal().getHeight();
        double scrollH = selectedPoi.getPosition().getX() / width;
        double scrollV = selectedPoi.getPosition().getY() / height;

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(500),
                new KeyValue(map_scrollpane.hvalueProperty(), scrollH),
                new KeyValue(map_scrollpane.vvalueProperty(), scrollV)
            )
        );
        timeline.play();

        map_pin.setLayoutX(selectedPoi.getPosition().getX());
        map_pin.setLayoutY(selectedPoi.getPosition().getY());
        pin_info.setText(selectedPoi.getDescription());
        map_pin.setVisible(true);
    }

    @FXML
    private void addPoi(MouseEvent event) {
        if (!event.isControlDown()) return;

        Dialog<Poi> dialog = new Dialog<>();
        dialog.setTitle("Nuevo POI");
        dialog.setHeaderText("Introduce un nuevo POI");

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Descripción...");
        descArea.setPrefRowCount(4);

        VBox vbox = new VBox(10, new Label("Nombre:"), nameField, new Label("Descripción:"), descArea);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Poi(nameField.getText().trim(), descArea.getText().trim(), 0, 0);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(poi -> {
            Point2D point = zoomGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
            poi.setPosition(point);
            map_listview.getItems().add(poi);
        });
    }

    // === Login ===
    @FXML
    private void onLogin(ActionEvent event) {
        Dialog<Pair<String,String>> dlg = new Dialog<>();
        dlg.setTitle("Iniciar sesión");
        dlg.setHeaderText("Introduce tus credenciales");

        TextField userField = new TextField();
        userField.setPromptText("Usuario");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Contraseña");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(userField,           1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passField,           1, 1);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(btn -> btn == ButtonType.OK
            ? new Pair<>(userField.getText().trim(), passField.getText())
            : null
        );

        dlg.showAndWait().ifPresent(creds -> {
          try {
            Navigation nav = Navigation.getInstance();
            User u = nav.authenticate(creds.getKey(), creds.getValue());
            if (u == null) {
              new Alert(Alert.AlertType.ERROR,
                        "Usuario o contraseña incorrectos",
                        ButtonType.OK)
                .showAndWait();
            } else {
              sesionIniciada.set(true);
            }
          } catch (NavDAOException e) {
            new Alert(Alert.AlertType.ERROR,
                      "Error de base de datos:\n" + e.getMessage(),
                      ButtonType.OK)
              .showAndWait();
          }
        });
    }
    
    @FXML
    private void onRegister(ActionEvent event) {
        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle("Registro de usuario");
        dlg.setHeaderText("Rellena tus datos");

        TextField nickField  = new TextField();
        nickField.setPromptText("Nickname (6–15 caracteres)");

        TextField emailField = new TextField();
        emailField.setPromptText("Email válido");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password (8–20 caracteres)");

        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Fecha de nacimiento");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nickname:"), 0, 0);
        grid.add(nickField,               1, 0);
        grid.add(new Label("Email:"),    0, 1);
        grid.add(emailField,             1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passField,              1, 2);
        grid.add(new Label("Nacimiento:"),0, 3);
        grid.add(dobPicker,              1, 3);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Disable OK until all fields are non-empty
        Node okButton = dlg.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(
          nickField.textProperty().isEmpty()
          .or(emailField.textProperty().isEmpty())
          .or(passField.textProperty().isEmpty())
          .or(dobPicker.valueProperty().isNull())
        );

        // Convert result and do all validation/registration here
        dlg.setResultConverter(btn -> {
          if (btn == ButtonType.OK) {
            // 1) Field-format checks
            if (!User.checkNickName(nickField.getText().trim()))
              throw new IllegalArgumentException("Nickname inválido");
            if (!User.checkEmail(emailField.getText().trim()))
              throw new IllegalArgumentException("Email inválido");
            if (!User.checkPassword(passField.getText()))
              throw new IllegalArgumentException("Password inválida");
            if (Period.between(dobPicker.getValue(), LocalDate.now()).getYears() < 16)
              throw new IllegalArgumentException("Debes tener al menos 16 años");

            // 2) Attempt to register
            try {
              Navigation nav = Navigation.getInstance();
              // (we’re skipping the existsNickName check here,
              // in case your Navigation API doesn’t expose it)
              return nav.registerUser(
                nickField.getText().trim(),
                emailField.getText().trim(),
                passField.getText(),
                null,                     // no avatar for now
                dobPicker.getValue()
              );
            } catch (NavDAOException e) {
              throw new RuntimeException("Error de base de datos: " + e.getMessage(), e);
            }
          }
          return null;
        });

        // Show dialog & handle success or validation/runtime errors
        try {
          Optional<User> result = dlg.showAndWait();
          result.ifPresent(u ->
            new Alert(Alert.AlertType.INFORMATION,
                      "Registro completado. Ya puedes hacer Log in.",
                      ButtonType.OK)
            .showAndWait()
          );
        } catch (RuntimeException ex) {
          new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK)
            .showAndWait();
        }
    }

    // === Información Acerca de ===
    @FXML
    private void about(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("IPC - 2025");
        alert.showAndWait();
    }

    // === Mostrar posición del ratón ===
    @FXML
    private void showPosition(MouseEvent event) {
        mousePosition.setText(
            "sceneX: " + (int) event.getSceneX() + ", sceneY: " + (int) event.getSceneY() +
            "\nX: " + (int) event.getX() + ", Y: " + (int) event.getY()
        );
    }

    @FXML
    private void center(ActionEvent event) {
        centrarContenido();
        zoom_slider.setValue(zoom_slider.getMin());
    }

  
    @FXML
    private void seleccionarAccion(ActionEvent event) throws NavDAOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDisplayProblems.fxml"));
            Parent root = loader.load();

            Stage nuevaVentana = new Stage();
            FXMLDisplayProblemsController controller = loader.getController();
            controller.setStage(nuevaVentana);
            nuevaVentana.setTitle("Seleccionar problema");
            nuevaVentana.setScene(new Scene(root));
            nuevaVentana.show();
            
            int i = controller.getIndex();
            if(i >= 0){
                obj = Navigation.getInstance();
                List<Problem> problemas = obj.getProblems();
                enunciadoPregunta.setText(problemas.get(i).getText());
                enunciadoPregunta.setVisible(true);
                List<Answer> opciones = problemas.get(i).getAnswers();
                ans1.setText(opciones.get(0).getText());
                ans2.setText(opciones.get(1).getText());
                ans3.setText(opciones.get(2).getText());
                ans4.setText(opciones.get(3).getText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void seleccionRandom(ActionEvent event) throws NavDAOException {
        obj = Navigation.getInstance();
        List<Problem> problemas = obj.getProblems();
        int i = (int)(Math.random() * problemas.size());
        enunciadoPregunta.setText(problemas.get(i).getText());
        enunciadoPregunta.setVisible(true);
        List<Answer> opciones = problemas.get(i).getAnswers();
        ans1.setText(opciones.get(0).getText());
        ans2.setText(opciones.get(1).getText());
        ans3.setText(opciones.get(2).getText());
        ans4.setText(opciones.get(3).getText());
    }

    @FXML
    private void addPuntito(MouseEvent event) {
        TextField texto = new TextField("X");
        zoomGroup.getChildren().add(texto);
        texto.setLayoutX(event.getX());
        texto.setLayoutY(event.getY());
        //texto.requestFocus();
    }

    @FXML
    private void puntoPulsado(ActionEvent event) {
        
    }

    @FXML
    private void handleMapClick(MouseEvent event) {
        
        
    
    }

    @FXML
    private void addTrans(ActionEvent event) {
      
    }

}