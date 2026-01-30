package gitgud.pfm.Controllers;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Button;


import java.net.URL;
import java.util.ResourceBundle;

import gitgud.pfm.App;
import gitgud.pfm.Models.Category;
import gitgud.pfm.services.CategoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CategoryController implements Initializable {
	@FXML private FlowPane customExpensePane;
	@FXML private FlowPane customIncomePane;
	// Dynamically add predefined categories as buttons
	// @FXML private ListView<String> categoryListView;
	@FXML private TextField nameField;
	@FXML private TextField descField;
	@FXML private ComboBox<String> typeCombo;

	private final CategoryService service = new CategoryService();
	public void initialize(URL location, ResourceBundle resources) {
		typeCombo.setItems(FXCollections.observableArrayList("EXPENSE", "INCOME"));
		updatePredefinedCategoryButtons();
	}

	private void updatePredefinedCategoryButtons() {
		customExpensePane.getChildren().clear();
		customIncomePane.getChildren().clear();
		for (Category c : service.getDefaultCategories()) {
			Button btn = new Button(c.getName());
			btn.setStyle("-fx-background-radius: 20; -fx-padding: 10 20; -fx-background-color: #b3d9ff;");
			btn.setOnAction(e -> showAlert("Edit Predefined Category: " + c.getName()));
			if (c.getType() == Category.Type.EXPENSE) {
				customExpensePane.getChildren().add(btn);
			} else {
				customIncomePane.getChildren().add(btn);
			}
		}
	}

	// private void updateCategoryList() {
	//     categoryNames = FXCollections.observableArrayList();
	//     for (Category c : service.getAllCategories()) {
	//         categoryNames.add(c.getName() + " (" + c.getType() + ")" + (c.isCustom() ? " [Custom]" : ""));
	//     }
	//     categoryListView.setItems(categoryNames);
	// }

	@FXML
	private void goBack() {
		try {
			App.setRoot("primary");
		} catch (Exception e) {
			showAlert("Failed to go back: " + e.getMessage());
		}
	}

	private void showAlert(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText(msg);
		alert.showAndWait();
	}
}
