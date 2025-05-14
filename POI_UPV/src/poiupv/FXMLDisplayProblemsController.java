/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package poiupv;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import model.Navigation;
import static model.Navigation.getInstance;
import model.Problem;
import model.NavDAOException;

/**
 * FXML Controller class
 *
 * @author clara
 */


public class FXMLDisplayProblemsController implements Initializable{
    
    //Navigation obj;
    private ObservableList<Problem> datos = null;
    private List<Problem> problemas;

    @FXML
    private ListView<Problem> listaProblemas;
    @FXML
    private Button seleccionarProblema;

    
    
    class ProblemListCell extends ListCell<Problem>{
        private final Label label = new Label();
        {
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            label.maxWidthProperty().bind(this.widthProperty().subtract(20));
        }
        
        @Override
        protected void updateItem(Problem item, boolean empty){
            super.updateItem(item, empty);
            if(item == null || empty) setGraphic(null);
            else {
                label.setText(item.getText());
                setGraphic(label);
            }
        }
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        try{
            Navigation obj = Navigation.getInstance();
            datos = listaProblemas.getItems();
            listaProblemas.setCellFactory(c -> new ProblemListCell());
            problemas = obj.getProblems();
            System.out.println(problemas.get(0));
            for(int i = 0; i < problemas.size(); i++){
                datos.add(problemas.get(i));
            }
                        
            seleccionarProblema.disableProperty().bind(Bindings.equal(listaProblemas.getSelectionModel().selectedIndexProperty(), -1));
        }
        catch(NavDAOException e){
            System.err.println(e);
        }
    }    

    @FXML
    private void seleccionarAccion(ActionEvent event) {
        
    }
    
}
