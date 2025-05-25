/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carta_navegacion;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import model.NavDAOException;
import model.Navigation;
import static model.Navigation.getInstance;
import model.Session;
import model.User;

/**
 *
 * @author clara
 */
public class FXMLShowStatsController implements Initializable {
    
    private User currentUser;
    private int hits, faults;
    
    private final ObservableList<Session> sesiones = FXCollections.observableArrayList();
    List<Session> historial;

    @FXML
    private HBox filtros;
    @FXML
    private TableView<Session> historialSesiones;
    @FXML
    private Text totalHits;
    @FXML
    private Text totalFaults;
    @FXML
    private Text hitRate;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableColumn<Session, LocalDateTime> fechaSesion;
    @FXML
    private TableColumn<Session, Integer> aciertosSesion;
    @FXML
    private TableColumn<Session, Integer> fallosSesion;
    @FXML
    private Button eliminarFiltro;
    
    
    public void setUser(User user){
        currentUser = user;
    }
    
    public void currentSession(int hits, int faults){
        this.hits = hits;
        this.faults = faults;
    }
    
    public void loadData(){
        try {
            Navigation obj = getInstance();
            historial = currentUser.getSessions();
            
            fechaSesion.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timeStamp"));
            aciertosSesion.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hits"));
            fallosSesion.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("faults"));

            
            historialSesiones.setItems(sesiones);
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if(newVal != null) historialSesiones.setItems(sesiones.filtered(s -> !s.getTimeStamp().isBefore(newVal.atStartOfDay())));
                else historialSesiones.setItems(sesiones);
            });
            
            Session sesionActual = new Session(LocalDateTime.now(), hits, faults);
            sesiones.clear();
            sesiones.add(sesionActual);
            sesiones.addAll(historial);
            sesiones.sort((a, b) -> b.getTimeStamp().compareTo(a.getTimeStamp()));
            
            int h = hits, f = faults;
            for(Session s : historial){
                h += s.getHits();
                f += s.getFaults();
            }
            double ta = 0;
            if(h + f > 0){
                ta = (h * 100.0 / (h + f));
            }
            totalHits.setText("Aciertos totales: " + h);
            totalFaults.setText("Fallos totales: " + f);
            hitRate.setText("Tasa de aciertos: " + String.format("%.2f%%", ta));
        } catch (NavDAOException ex) {
            Logger.getLogger(FXMLShowStatsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eliminarFiltro.visibleProperty().bind(datePicker.valueProperty().isNotNull());
        
    }

    @FXML
    private void quitarFiltros(ActionEvent event) {
        datePicker.setValue(null);
        historialSesiones.setItems(sesiones);
        Session sesionActual = new Session(LocalDateTime.now(), hits, faults);
        sesiones.clear();
        sesiones.add(sesionActual);
        sesiones.addAll(historial);
        sesiones.sort((a, b) -> b.getTimeStamp().compareTo(a.getTimeStamp()));
    }
    
}
