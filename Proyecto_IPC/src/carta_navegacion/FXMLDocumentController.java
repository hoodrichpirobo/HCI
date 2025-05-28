package carta_navegacion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
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
import javafx.beans.binding.BooleanBinding;
import model.Navigation;
import model.Problem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.paint.Color;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Answer;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Arc;
import javafx.stage.FileChooser;
import javafx.scene.shape.Circle;


import javafx.scene.shape.Line;

import javafx.scene.text.Font;
import model.User;


/**
 * Controlador para:
 * - Mapa con zoom y puntos de interés (POI)
 * - Sección de preguntas tipo test
 * - Login mediante diálogo
 */
public class FXMLDocumentController implements Initializable {

    Navigation obj;
    public User currentUser = null;
    public IntegerProperty hits = new SimpleIntegerProperty(0), faults = new SimpleIntegerProperty(0);
    // Lista de respuestas de la pregunta cargada (en el mismo orden en que pintas los RadioButton)
    private List<Answer> currentAnswers = Collections.emptyList();
    // === Campos FXML ===
    @FXML private ListView<Poi> map_listview;
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
    @FXML
    private RadioButton ans1;
    @FXML
    private RadioButton ans2;
    @FXML
    private RadioButton ans3;
    @FXML
    private RadioButton ans4;
    @FXML
    private Button botonEnviar;
    @FXML
    private Button borrarSeleccion;
    @FXML
    private ScrollPane scrollTest;

    // === Estado interno ===
    private Group zoomGroup;
    public final BooleanProperty sesionIniciada = new SimpleBooleanProperty(false);
    private ChangeListener<Number> bloqueoDivisor;
    @FXML
    private Button centerButton;
    @FXML
    private ToggleButton transButton;
    @FXML private Label lblUser;
    @FXML
    private Button menos;
    @FXML
    private Button mas;
    @FXML
    private Button stats;
    @FXML
    private Text displayHits;
    @FXML
    private Text displayFaults;
    @FXML
    private ImageView transportador;
    @FXML
    private Pane mapPane;
    @FXML
    private ToggleButton botonPunto;
    @FXML
    private VBox modificadoresObjetos;
    @FXML
    private Button papelera;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Group dibujar;
    @FXML
    private Slider rotate;
    @FXML
    private ImageView regla;
    @FXML
    private ToggleButton reglaBoton;
    @FXML
    private ToggleButton arcoBoton;
    

    @FXML
    private ImageView mapa;
    @FXML private ImageView avatarView;                                // ─── AVATAR (NEW)
    private static final String DEFAULT_AVATAR_RES = "/resources/default_avatar.png";   // ─── AVATAR
    private static final Path   AVATAR_DIR        = Paths.get("avatars");              // ─── AVATAR
    @FXML
    private MenuButton menuEditar;
    @FXML
    private RadioMenuItem transEdit;
    @FXML
    private RadioMenuItem rullerEdit;

    @FXML
    private ButtonBar barraEditar;


    @FXML
    private Slider tamano;

    @FXML
    private ToggleButton botonLinea;
    private Spinner<Double> elegirAngulo;
    private Slider sliderSize;
    private Spinner<Integer> elegirSize;
    @FXML
    private ToggleButton botonTexto;
    @FXML
    private ToggleButton botonGoma;
    @FXML
    private ImageView fotoGiro;
    @FXML
    private ImageView fotoAumento;
    @FXML
    private Spinner<Double> spinnerGrosor;
    @FXML
    private ToggleButton circuloBoton;
    @FXML
    TextField texto;
    @FXML
    private Button clear;
  
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        /* ─── 0.  MAP SCROLLPANE BASIC SETUP (from ca0667…) ──────────────── */
        mapPane.setPrefSize(2500, 1700);                       // give the pane room
        map_scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        map_scrollpane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        /* ─── 1.  ZOOM SLIDER (from HEAD) ────────────────────────────────── */
        zoom_slider.setMin(0.015);
        zoom_slider.setMax(1.5);
        zoom_slider.setValue(0.5);
        zoom_slider.valueProperty().addListener(
            (obs, ov, nv) -> applyZoom(nv.doubleValue()));

        /*  Put the chart inside a Group so we can scale it  */
        Group contentGroup = new Group();
        zoomGroup = new Group();
        zoomGroup.getChildren().add(map_scrollpane.getContent());
        contentGroup.getChildren().add(zoomGroup);
        map_scrollpane.setContent(contentGroup);
        centrarContenido();
        //configurarContenidoMapa();          // centres & applies initial 0.1 scale

        /* ─── 2.  HIDE QUESTION PANEL UNTIL LOGIN ────────────────────────── */
        configurarSeccionPreguntas();

        /* ─── 3.  BUTTON & LABEL BINDINGS ────────────────────────────────── */
        stats.visibleProperty().bind(sesionIniciada);
        registerButton.disableProperty().bind(sesionIniciada);

        loginButton.textProperty().bind(
            Bindings.when(sesionIniciada).then("Log out").otherwise("Log in"));

        lblUser.setText("");
        avatarView.setImage(null);          // start with blank avatar

        sesionIniciada.addListener((obs, wasIn, isIn) -> {
            if (isIn && currentUser != null) {          // user just logged-in
                lblUser.setText(currentUser.getNickName());
                refreshAvatar(currentUser.getAvatar());
            } else {                                    // user logged-out
                lblUser.setText("");
                avatarView.setImage(null);              // clear icon
            }
        });

        /* ─── 4.  TOOLS (protractor, ruler, etc.) ────────────────────────── */
        /*elegirAngulo.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 360, 0));
        elegirSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 5));
        rotate.valueProperty().addListener((obs, oldVal, newVal) -> {
            elegirAngulo.getValueFactory().setValue(newVal.doubleValue());
        });
        elegirAngulo.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotate.setValue(newVal);
        });
        sliderSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            elegirSize.getValueFactory().setValue(newVal.intValue());
        });
        elegirSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            sliderSize.setValue(newVal);
        });
        */
        ToggleGroup dibujos = new ToggleGroup();
        botonLinea.setToggleGroup(dibujos);
        botonPunto.setToggleGroup(dibujos);
        circuloBoton.setToggleGroup(dibujos);
        botonTexto.setToggleGroup(dibujos);
        botonGoma.setToggleGroup(dibujos);
        arcoBoton.setToggleGroup(dibujos);
        configurarTransportador();
        configurarRegla();

        spinnerGrosor.setValueFactory(getGrosor());

        spinnerGrosor.setEditable(true);
        
        barraEditar.setVisible(false);
        
       
        menuEditar.disableProperty().bind(
            Bindings.not(
                    transButton.selectedProperty().or(reglaBoton.selectedProperty())
            )
        );
            
        transEdit.setOnAction(e -> {
            if(transButton.isSelected()){
                rullerEdit.setSelected(false);
                barraEditar.setVisible(true);
                editarReglas();
            }
        });
        rullerEdit.setOnAction(e -> {
            transEdit.setSelected(false);
            if(reglaBoton.isSelected()){
                barraEditar.setVisible(true);
                fotoAumento.setVisible(false);
                tamano.setVisible(false);
                editarReglas();
            }
        });
        
        circuloBoton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
        if (isNowSelected) {
            mapa.setOnMousePressed(this::ponerCentro);
            mapa.setOnMouseDragged(this::ponerRadio);
        } else {
            mapa.setOnMousePressed(null);
            mapa.setOnMouseDragged(null);
        }
    });
        arcoBoton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
        if (isNowSelected) {
            mapa.setOnMousePressed(this::ponerCentroArco);
            mapa.setOnMouseDragged(this::ponerRadioArco);
        } else {
            mapa.setOnMousePressed(null);
            mapa.setOnMouseDragged(null);
        }
    });
         botonTexto.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
        if (isNowSelected) {
            spinnerGrosor.setValueFactory(getGrosor());
            mapa.setOnMousePressed(this::colocarTexto);

        } else {
            mapa.setOnMousePressed(null);
        }
    });
         
    }
    
    
       
    
    /* ───────────────  AVATAR helper  ─────────────── */
    private void refreshAvatar(Image img) {                            // ─── AVATAR
        if (img != null) { avatarView.setImage(img); return; }

        InputStream is = getClass().getResourceAsStream(DEFAULT_AVATAR_RES);
        if (is != null) avatarView.setImage(new Image(is));
    }
    private SpinnerValueFactory.DoubleSpinnerValueFactory getGrosor(){
        if(botonTexto.isSelected()){
            return new SpinnerValueFactory.DoubleSpinnerValueFactory(50.0, 300.0, 100.0, 10.0); // min, max, initial, step
        }else{
             return new SpinnerValueFactory.DoubleSpinnerValueFactory(5.0, 30.0, 5.0, 1.0); 
        }
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
            enunciadoPregunta.setVisible(false);
            ans1.setVisible(false);
            ans2.setVisible(false);
            ans3.setVisible(false);
            ans4.setVisible(false);
            botonEnviar.setVisible(false);
            borrarSeleccion.setVisible(false);
            scrollTest.setVisible(false);
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
                    scrollTest.setVisible(true);
                    displayHits.textProperty().bind(hits.asString("Hits %d"));
                    displayFaults.textProperty().bind(faults.asString("Faults: %d"));
                } else {
                    splitPane.setDividerPositions(0.0);
                    splitPane.getDividers().get(0).positionProperty().addListener(bloqueoDivisor);
                    seccionPreguntas.setVisible(false);
                    scrollTest.setVisible(false);
                }
            });

            seccionPreguntas.widthProperty().addListener((obs, oldVal, newVal) -> {
                enunciadoPregunta.setWrappingWidth(newVal.doubleValue() - 20); // deja margen
                ans1.setWrapText(true);
                ans1.maxWidthProperty().bind(seccionPreguntas.widthProperty().subtract(20));
                ans2.setWrapText(true);
                ans2.maxWidthProperty().bind(seccionPreguntas.widthProperty().subtract(20));
                ans3.setWrapText(true);
                ans3.maxWidthProperty().bind(seccionPreguntas.widthProperty().subtract(20));
                ans4.setWrapText(true);
                ans4.maxWidthProperty().bind(seccionPreguntas.widthProperty().subtract(20));
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
            "          " + 
            "X: " + (int) event.getX() + ", Y: " + (int) event.getY()
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
            nuevaVentana.setOnHidden(e -> {
                int i = controller.getIndex();
                if(i >= 0){
                    try {
                        obj = Navigation.getInstance();
                    } catch (NavDAOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    List<Problem> problemas = obj.getProblems();
                    enunciadoPregunta.setText(problemas.get(i).getText());
                    enunciadoPregunta.setVisible(true);
                    ans1.setVisible(true);
                    ans2.setVisible(true);
                    ans3.setVisible(true);
                    ans4.setVisible(true);
                    esperarRespuesta();
                    botonEnviar.setVisible(true);
                    borrarSeleccion.setVisible(true);
                    List<Answer> opciones = new ArrayList<>(problemas.get(i).getAnswers());
                    Collections.shuffle(opciones);
                    currentAnswers = opciones;
                    ans1.setText(opciones.get(0).getText());
                    ans2.setText(opciones.get(1).getText());
                    ans3.setText(opciones.get(2).getText());
                    ans4.setText(opciones.get(3).getText());
                }
            });
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
        ans1.setVisible(true);
        ans2.setVisible(true);
        ans3.setVisible(true);
        ans4.setVisible(true);
        esperarRespuesta();
        botonEnviar.setVisible(true);
        borrarSeleccion.setVisible(true);
        List<Answer> opciones = new ArrayList<>(problemas.get(i).getAnswers());
        Collections.shuffle(opciones);
        currentAnswers = opciones;
        ans1.setText(opciones.get(0).getText());
        ans2.setText(opciones.get(1).getText());
        ans3.setText(opciones.get(2).getText());
        ans4.setText(opciones.get(3).getText());
    }
    
    private void esperarRespuesta(){
        ToggleGroup opciones = new ToggleGroup();
        ans1.setToggleGroup(opciones);
        ans2.setToggleGroup(opciones);
        ans3.setToggleGroup(opciones);
        ans4.setToggleGroup(opciones);
        botonEnviar.setDisable(true);
        borrarSeleccion.setDisable(true);
        opciones.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            botonEnviar.setDisable(newVal == null);
            borrarSeleccion.setDisable(newVal == null);
        });
        borrarSeleccion.setOnAction(e -> {
            opciones.selectToggle(null);
        });
    }
    DropShadow glow = new DropShadow(10, Color.GREEN);
    List<Node> dibujos = new ArrayList<>();
    //Circle puntoSeleccionado = null;
    Point2D ini = null, fin = null;
    Line latitud = null, longitud = null;
    private Node nodoSeleccionado = null;
    Line linea = null;
    @FXML
    private void handleMapClick(MouseEvent event) {
        if(nodoSeleccionado != null) {
            nodoSeleccionado.setEffect(null);
            nodoSeleccionado = null;
        }
        if(latitud != null) dibujar.getChildren().remove(latitud);
        if(longitud != null) dibujar.getChildren().remove(longitud);
        if(event.isConsumed()) return;
        else if(botonPunto.isSelected()){
            //puntoSeleccionado.fillProperty().unbind();
            //puntoSeleccionado.strokeProperty().unbind();
            double x = event.getX(), y = event.getY();
            Point2D p = new Point2D(x, y);
            Circle c = new Circle(p.getX(), p.getY(), 5);
            c.setFill(colorPicker.getValue());
            c.setStroke(colorPicker.getValue());
            dibujar.getChildren().add(c);
            dibujos.add(c);
            c.setOnMouseClicked(e -> {
                e.consume();
                if(nodoSeleccionado != null){
                    nodoSeleccionado.setEffect(null);
                }
                nodoSeleccionado = c;
                colorPicker.setValue((Color)((Circle)nodoSeleccionado).getFill());
                //sliderSize.adjustValue(((Circle)nodoSeleccionado).getRadius());
                //puntoSeleccionado.fillProperty().bind(colorPicker.valueProperty());
                //puntoSeleccionado.strokeProperty().bind(colorPicker.valueProperty());
                nodoSeleccionado.setEffect(glow);
                
                latitud = new Line(e.getSceneX(), 0, e.getSceneX(), 2700);
                longitud = new Line(0, e.getSceneY(), 2000, e.getSceneY());
                latitud.setStroke(Color.GRAY);
                longitud.setStroke(Color.GRAY);
                latitud.setStrokeWidth(1);
                longitud.setStrokeWidth(1);
                dibujar.getChildren().add(latitud);
                dibujar.getChildren().add(longitud);
                
                colorPicker.setOnAction(h -> {
                   Color color = colorPicker.getValue();
                   ((Circle)nodoSeleccionado).setFill(color);
                   ((Circle)nodoSeleccionado).setStroke(color);
                });
                /*sliderSize.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if(nodoSeleccionado != null){
                        ((Circle)nodoSeleccionado).setRadius(newVal.doubleValue());
                    }
                });*/
            });
            c.setOnMouseDragged(g -> {
                g.consume();
                double dx = g.getSceneX(), dy = g.getSceneY();
                /*c.setCenterX(c.getCenterX() + dx);
                c.setCenterY(c.getCenterY() + dy);
                Point2D cp = new Point2D(puntoSeleccionado.getCenterX(), puntoSeleccionado.getCenterY());
                Point2D dp = new Point2D(dx, dy);*/
                nodoSeleccionado.setTranslateX(Math.clamp(((Circle)nodoSeleccionado).getCenterX(), 150, 1100));
                nodoSeleccionado.setTranslateY(Math.clamp(((Circle)nodoSeleccionado).getCenterY(), 300, 600));
            });
        }
        else if(botonLinea.isSelected()){
            double x = event.getX(), y = event.getY();
            if(ini == null){
                ini = new Point2D(x,y);
                Circle c = new Circle(x, y, 3, Color.MAGENTA);
                dibujar.getChildren().add(c);
                dibujos.add(c);
            }
            else{
                fin = new Point2D(x,y);
                linea = new Line(ini.getX(), ini.getY(), x, y);
                linea.setStroke(Color.MAGENTA);
                linea.setStrokeWidth(2);
                dibujar.getChildren().add(linea);
                dibujos.add(linea);
                Circle c = new Circle(x, y, 3, Color.MAGENTA);
                dibujar.getChildren().add(c);
                dibujos.add(c);
                ini = fin = null;
            }
            if(linea != null){
                linea.setOnMouseClicked(e -> {
                    e.consume();
                    if(nodoSeleccionado != null){
                        nodoSeleccionado.setEffect(null);
                    }
                    nodoSeleccionado = linea;
                    nodoSeleccionado.setEffect(glow);
                });
            }
        }
        clear.setDisable(nodoSeleccionado == null);
    }
    private void editarReglas(){
        if(transEdit.isSelected()){
            transportador.setEffect(glow);
            rotate.valueProperty().unbind();
            transportador.rotateProperty().bind(rotate.valueProperty());
            
            
            System.out.println(tamano.getMin() + "");
            System.out.println(tamano.getMax() + "");
            tamano.setMin(500);
            tamano.setMax(3000);
            tamano.valueProperty().unbind();
            transportador.fitWidthProperty().bind(tamano.valueProperty());
            transportador.fitHeightProperty().bind(tamano.valueProperty());   
        }
        else{
            transportador.setEffect(null);
        }
        if(rullerEdit.isSelected()){
            regla.setEffect(glow);
            rotate.valueProperty().unbind();
            regla.rotateProperty().bind(rotate.valueProperty());
        }
        else{
            regla.setEffect(null);
        }
        
    }
    private void configurarRegla(){
        
        regla.setVisible(false);
        regla.setX(1500);
        regla.setY(3000);
        regla.setFitWidth(5000);
        regla.setFitHeight(5000);
        
        
        regla.visibleProperty().bind(reglaBoton.selectedProperty());

        regla.setOnMousePressed(this::cogerRegla);
        regla.setOnMouseDragged(this::moverRegla);
    
    }
    private void configurarTransportador() {
        
        transportador.setX(3000);
        transportador.setY(3000);

        //transportador.setPreserveRatio(true);
        transportador.setFitWidth(2500);
        transportador.setFitHeight(2500);

        transportador.setVisible(false);

        transportador.rotateProperty().bind(rotate.valueProperty());
        transportador.visibleProperty().bind(transButton.selectedProperty());

        transportador.setOnMousePressed(this::cogerTransportador);
        transportador.setOnMouseDragged(this::moverTransportador);
        
        
    }

    @FXML
    private void enviarRespuesta(ActionEvent event) {
        // 1) Averigua qué radioButton está seleccionado
        Toggle selected = ans1.getToggleGroup().getSelectedToggle();
        if (selected == null) return;

        RadioButton elegido = (RadioButton) selected;
        int idx = ans1.getToggleGroup().getToggles().indexOf(selected);

        // 2) Comprueba si acertó
        Answer respuesta = currentAnswers.get(idx);
        if (respuesta.getValidity()) {
            hits.set(hits.get() + 1);
            new Alert(Alert.AlertType.INFORMATION, "¡Correcto!", ButtonType.OK).showAndWait();
        } else {
            faults.set(faults.get() + 1);
            new Alert(Alert.AlertType.ERROR, "Incorrecto…", ButtonType.OK).showAndWait();
        }

        // 3) Deshabilita el botón de enviar hasta nueva selección
        ans1.getToggleGroup().selectToggle(null);
    }

    @FXML
    private void showStats(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLShowStats.fxml"));
        Parent root = loader.load();
        FXMLShowStatsController controller = loader.getController();
        controller.setUser(currentUser);
        controller.currentSession(hits.get(), faults.get());
        controller.loadData();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
        
        
        /*Alert estadisticas = new Alert(AlertType.INFORMATION);
        estadisticas.setHeaderText("Estadísticas globales de " + currentUser.getNickName());
        List<Session> a = currentUser.getSessions();
        int h = hits.get(), f = faults.get();
        for(Session s : a){
            h += s.getHits();
            f += s.getFaults();
        }
        double ta = 0;
        if(h + f > 0){
            ta = (h * 100.0 / (h + f));
        }
        estadisticas.setContentText("Total Hits: " + h + "\n" + "Total Faults: " + f + "\n" + "Tasa de aciertos: " + ta + "%");
        estadisticas.showAndWait();*/
    }

    double baseX, baseY, bx, by;  
    Point2D localBase;
    Point2D localBaseRegla;
    //@FXML
    //private void moverTransportador(MouseEvent event) {    Point2D localBase;
    //Point2D localBaseRegla;
    @FXML
    private void moverTransportador(MouseEvent event) {
        map_scrollpane.setPannable(false); 
        
        Point2D localPos = zoomGroup.sceneToLocal(event.getSceneX(),event.getSceneY());
        transportador.setTranslateX(Math.clamp((baseX + localPos.getX() - localBase.getX()),-3000,5000));
        transportador.setTranslateY(Math.clamp((baseY + localPos.getY() - localBase.getY()),-3000,2000));
        event.consume();

    }

    @FXML
    private void cogerTransportador(MouseEvent event) {
        map_scrollpane.setPannable(false); 
        
       localBase = zoomGroup.sceneToLocal(event.getSceneX() , event.getSceneY());
       baseX = transportador.getTranslateX();
       baseY = transportador.getTranslateY();
       event.consume();
    }
    
    @FXML
    private void cogerRegla(MouseEvent event){
        map_scrollpane.setPannable(false); 
    
        localBaseRegla = zoomGroup.sceneToLocal(event.getSceneX() , event.getSceneY());

        bx = regla.getTranslateX();
        by = regla.getTranslateY();

        event.consume();
        
    }
    
    @FXML
     private void moverRegla(MouseEvent event) {

         map_scrollpane.setPannable(false); 
        
        
        Point2D localPos = zoomGroup.sceneToLocal(event.getSceneX(),event.getSceneY());
        regla.setTranslateX(Math.clamp(bx + localPos.getX() - localBaseRegla.getX(), -2200,2250));
        regla.setTranslateY(Math.clamp(by + localPos.getY() - localBaseRegla.getY(), -3000, 2250));
        event.consume();
    }


    @FXML
    private void clearAll(ActionEvent event) {
        dibujar.getChildren().clear();
        dibujos.clear();
    }

    private void ponerRadio(MouseEvent event) {
        Point2D localPoint = zoomGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
        double radio = Math.sqrt(Math.pow(localPoint.getX() - inicioXarc, 2) +
                             Math.pow(localPoint.getY() - inicioYarc, 2));
        circlePainting.setRadius(radio);
        event.consume();
    }
    
    Circle circlePainting;
    double inicioXarc, inicioYarc;
    private void ponerCentro(MouseEvent event) {
            Point2D localPoint = zoomGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
            
            circlePainting = new Circle(1); 
            circlePainting.setStroke(Color.RED);
            circlePainting.setFill(Color.TRANSPARENT);
            circlePainting.setStroke(colorPicker.getValue());
            circlePainting.setStrokeWidth(spinnerGrosor.getValue());

            
            
            
            
            circlePainting.setCenterX(localPoint.getX());
            circlePainting.setCenterY(localPoint.getY());
            
            inicioXarc = localPoint.getX();
            inicioYarc = localPoint.getY();
        
            dibujar.getChildren().add(circlePainting);
            dibujos.add(circlePainting);
    }
    Arc arcPainting;
    double inicioXarco, inicioYarco;
    private void ponerCentroArco(MouseEvent event) {
            Point2D localPoint = zoomGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
            
            arcPainting = new Arc(); 
            arcPainting.setFill(Color.TRANSPARENT);
            arcPainting.setStroke(colorPicker.getValue());
            arcPainting.setStrokeWidth(spinnerGrosor.getValue());   
            arcPainting.setCenterX(localPoint.getX());
            arcPainting.setCenterY(localPoint.getY());
            arcPainting.setLength(180);   

            
            inicioXarco = localPoint.getX();
            inicioYarco = localPoint.getY();
        
            dibujar.getChildren().add(arcPainting);
            dibujos.add(arcPainting);
    }
    private void ponerRadioArco(MouseEvent event) {
        Point2D localPoint = zoomGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
      
        double rx = Math.abs(localPoint.getX() - inicioXarco);
        double ry = Math.abs(localPoint.getY() - inicioYarco);
        double radio = Math.sqrt(rx*rx + ry*ry);
        arcPainting.setRadiusX(radio);
        arcPainting.setRadiusY(radio);
        event.consume();
        
    }
    
    private void colocarTexto(MouseEvent event) {
            Point2D localPoint = zoomGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
            texto = new TextField();
            // Añadimos el texto al contenedor, lo posicionamos donde está el ratón y muy importante, pedimos el foco.
            zoomGroup.getChildren().add(texto);
            texto.setLayoutX(localPoint.getX());
            texto.setLayoutY(localPoint.getY());
            texto.requestFocus();
            
            texto.setOnAction(e -> {
                Text textoT = new Text(texto.getText());
                textoT.setX(texto.getLayoutX());
                textoT.setY(texto.getLayoutY());
                double sizeDouble = spinnerGrosor.getValue();
                int size = (int) sizeDouble;
                
                textoT.setFont(Font.font("Gafata", size));
                
                textoT.setFill(colorPicker.getValue());
                
               
                dibujar.getChildren().add(textoT);
                dibujos.add(textoT);
                zoomGroup.getChildren().remove(texto);
                e.consume();
            });
            
    }
    private void cambiarEstiloTexto(TextField t){
    
    }
    @FXML
    private void addPoi(MouseEvent event) {
    }

   
   
     

    /* --------------------------------------------------------------------- */
    /*  HELPER : self-contained eye-toggle password input (emoji version)    */
    /* --------------------------------------------------------------------- */
    @SuppressWarnings("InnerClassMayBeStatic")
    private class PasswordInput {
        final PasswordField hidden = new PasswordField();
        final TextField     visible = new TextField();
        final ToggleButton  eyeBtn  = new ToggleButton("👁"); // ← no image file
        final HBox          box     = new HBox(4);

        PasswordInput(String prompt) {
            hidden.setPromptText(prompt);
            visible.setPromptText(prompt);

            // keep the two fields in sync
            visible.textProperty().bindBidirectional(hidden.textProperty());

            // show/hide
            visible.visibleProperty().bind(eyeBtn.selectedProperty());
            visible.managedProperty().bind(eyeBtn.selectedProperty());
            hidden.visibleProperty().bind(eyeBtn.selectedProperty().not());
            hidden.managedProperty().bind(eyeBtn.selectedProperty().not());

            box.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().addAll(hidden, visible, eyeBtn);
        }
        String getText() { return hidden.getText(); }
    }

    /* ===================================================================== */
    /*  LOGIN                                                                */
    /* ===================================================================== */
    @FXML
    private void onLogin(ActionEvent event) {

        /* A.  Si ya hay sesión, cerramos sesión ---------------------------- */
        if (sesionIniciada.get()) {
            if (currentUser != null) {
                currentUser.addSession(hits.get(), faults.get());
                hits.set(0);  faults.set(0);
            }
            currentUser = null;
            sesionIniciada.set(false);

            avatarView.setImage(null);          // ← CLEAR AVATAR
            lblUser.setText("");                // opcional: borra el nick

            seccionPreguntas.setVisible(false);
            splitPane.setDividerPositions(0.0);
            return;
        }

        /* B.  Diálogo de autenticación ------------------------------------ */
        Dialog<Pair<String,String>> dlg = new Dialog<>();
        dlg.setTitle("Iniciar sesión");
        dlg.setHeaderText("Introduce tus credenciales");

        TextField userField = new TextField();
        userField.setPromptText("Usuario");

        PasswordInput pwd = new PasswordInput("Contraseña");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Usuario:"),    userField);
        grid.addRow(1, new Label("Contraseña:"), pwd.box);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(btn ->
            btn == ButtonType.OK ? new Pair<>(userField.getText().trim(), pwd.getText()) : null);

        dlg.showAndWait().ifPresent(creds -> {
            try {
                Navigation nav = Navigation.getInstance();
                User u = nav.authenticate(creds.getKey(), creds.getValue());

                if (u == null) {
                    new Alert(Alert.AlertType.ERROR,
                              "Usuario o contraseña incorrectos", ButtonType.OK).showAndWait();
                } else {
                    currentUser = u;
                    sesionIniciada.set(true);
                    refreshAvatar(u.getAvatar());          // muestra avatar o default
                }
            } catch (NavDAOException e) {
                new Alert(Alert.AlertType.ERROR,
                          "Error de base de datos:\n" + e.getMessage(), ButtonType.OK).showAndWait();
            }
        });
    }

    /* ===================================================================== */
    /*  REGISTER                                                             */
    /* ===================================================================== */
    @FXML
    private void onRegister(ActionEvent event) {

        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle("Registro de usuario");
        dlg.setHeaderText("Rellena tus datos");

        TextField     nickField  = new TextField();
        TextField     emailField = new TextField();
        PasswordInput pwd        = new PasswordInput("Password (8–20 caracteres)");
        DatePicker    dobPicker  = new DatePicker();

        nickField.setPromptText("Nickname (6–15 caracteres)");
        emailField.setPromptText("Email válido");
        dobPicker.setPromptText("Fecha de nacimiento");

        Label passHelp = new Label(
            "Debe tener 8-20 caracteres, 1 mayúscula, 1 minúscula, "
          + "1 dígito y 1 especial (!@#$%&*()-+=)");
        passHelp.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Nickname:"),    nickField);
        grid.addRow(1, new Label("Email:"),       emailField);
        grid.addRow(2, new Label("Password:"),    pwd.box);
        grid.add(passHelp, 0, 3, 2, 1);
        grid.addRow(4, new Label("Nacimiento:"),  dobPicker);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes()
                           .addAll(ButtonType.OK, ButtonType.CANCEL);

        /* habilitar OK solo cuando los formatos son correctos --------------- */
        Node okBtn = dlg.getDialogPane().lookupButton(ButtonType.OK);
        BooleanBinding pwOk = Bindings.createBooleanBinding(
            () -> User.checkPassword(pwd.getText()),
            pwd.hidden.textProperty());
        okBtn.disableProperty().bind(
            nickField.textProperty().isEmpty()
              .or(emailField.textProperty().isEmpty())
              .or(dobPicker.valueProperty().isNull())
              .or(pwOk.not()));

        /* ------------- RESULT CONVERTER CON CONTROL DE DUPLICADOS ---------- */
        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            String nick = nickField.getText().trim();
            String mail = emailField.getText().trim();
            LocalDate dob = dobPicker.getValue();

            // 1) Validación de nickname
            if (!User.checkNickName(nick)) {
                new Alert(Alert.AlertType.ERROR,
                          "Nickname inválido – debe tener 6-15 caracteres alfanuméricos, guiones o guiones bajos.",
                          ButtonType.OK).showAndWait();
                return null;
            }
            // 2) Validación de email
            if (!User.checkEmail(mail)) {
                new Alert(Alert.AlertType.ERROR,
                          "Email inválido – introduce un correo con formato correcto.",
                          ButtonType.OK).showAndWait();
                return null;
            }
            // 3) Validación de edad mínima (16 años)
            if (dob == null || Period.between(dob, LocalDate.now()).getYears() < 16) {
                new Alert(Alert.AlertType.ERROR,
                          "Debes tener al menos 16 años para registrarte.",
                          ButtonType.OK).showAndWait();
                return null;
            }

            // 4) Intentar registro en BD (coge NavDAOException si ya existe nick)
            try {
                return Navigation.getInstance()
                         .registerUser(nick, mail, pwd.getText(), null, dob);
            } catch (NavDAOException ex) {
                if (ex.getMessage() != null &&
                    ex.getMessage().toLowerCase().contains("primary key")) {
                    new Alert(Alert.AlertType.ERROR,
                              "El nickname \"" + nick + "\" ya está en uso. Elige otro.",
                              ButtonType.OK).showAndWait();
                    return null;
                }
                throw new RuntimeException("Error BD: " + ex.getMessage(), ex);
            }
        });

        /* --------------------------- mostrar diálogo ----------------------- */
        try {
            dlg.showAndWait().ifPresent(u -> {
                currentUser = u;
                sesionIniciada.set(true);
                refreshAvatar(u.getAvatar());                              // ─── AVATAR
                new Alert(Alert.AlertType.INFORMATION,
                          "¡Bienvenido, " + u.getNickName() + "!",
                          ButtonType.OK).showAndWait();
            });
        } catch (RuntimeException ex) {   // recoge IllegalArgumentException + otros
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }




    /* ===================================================================== */
    /*  MODIFY PROFILE                                                       */
    /* ===================================================================== */
    @FXML
    private void onModifyProfile(ActionEvent event) {
        if (!sesionIniciada.get() || currentUser == null) {
            new Alert(Alert.AlertType.WARNING,
                      "Tienes que iniciar sesión primero", ButtonType.OK).showAndWait();
            return;
        }

        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle("Modificar perfil");
        dlg.setHeaderText("Actualiza tu información");

        TextField     emailField = new TextField(currentUser.getEmail());
        PasswordInput pwd        = new PasswordInput("Nueva contraseña");
        DatePicker    dobPicker  = new DatePicker(currentUser.getBirthdate());
        Button changeAvBtn = new Button("Cambiar avatar");             // ─── AVATAR
        
        /* pick & copy avatar on click ------------------------------------- */
        changeAvBtn.setOnAction(ev->{
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes","*.png","*.jpg","*.jpeg","*.gif"));
            File f = fc.showOpenDialog(dlg.getOwner());
            if (f!=null) {
                try {
                    Files.createDirectories(AVATAR_DIR);
                    Path target = AVATAR_DIR.resolve(
                        currentUser.getNickName()+"_"+f.getName());
                    Files.copy(f.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                    Image img = new Image(target.toUri().toString());
                    currentUser.setAvatar(img);                        // guarda en BD
                    refreshAvatar(img);                                // refresca inmediatamente
                } catch (Exception ex) {
                    new Alert(AlertType.ERROR,"No se pudo copiar imagen").showAndWait();
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Email:"),      emailField);
        grid.addRow(1, new Label("Password:"),   pwd.box);
        grid.addRow(2, new Label("Nacimiento:"), dobPicker);
        grid.addRow(3,new Label("Avatar:"), changeAvBtn);                 // ─── AVATAR

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okBtn = dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> !User.checkEmail(emailField.getText().trim())
                   || (!pwd.getText().isEmpty() && !User.checkPassword(pwd.getText()))
                   || dobPicker.getValue() == null
                   || Period.between(dobPicker.getValue(), LocalDate.now()).getYears() < 16,
                emailField.textProperty(), pwd.hidden.textProperty(), dobPicker.valueProperty())
            .not().not()   // double-negation → same as the big OR above
        );

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            currentUser.setEmail(emailField.getText().trim());
            if (!pwd.getText().isEmpty()) currentUser.setPassword(pwd.getText());
            currentUser.setBirthdate(dobPicker.getValue());
            return currentUser;
        });

        dlg.showAndWait().ifPresent(u ->
            new Alert(Alert.AlertType.INFORMATION,
                      "Perfil actualizado", ButtonType.OK).showAndWait());
    }
    
    @FXML
    private void addPunto(ActionEvent event) {
        
    }

    @FXML
    private void borrarObjeto(ActionEvent event) {
        if(nodoSeleccionado != null){
            dibujar.getChildren().remove(nodoSeleccionado);
            dibujos.remove(nodoSeleccionado);
            nodoSeleccionado = null;
            papelera.setDisable(true);
        }
    }
    
  
}