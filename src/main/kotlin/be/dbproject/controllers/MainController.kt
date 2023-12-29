package be.dbproject.controllers

import be.dbproject.ProjectMain
import be.dbproject.models.database.*
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Modality
import javafx.stage.Stage
import kotlin.reflect.KClass

class MainController {

    @FXML
    private lateinit var btnItemDetails: Button

    @FXML
    private lateinit var btnLocationDetails: Button

    @FXML
    private lateinit var btnVisitorDetails: Button

    @FXML
    private lateinit var btnManageGenres: Button

    @FXML
    private lateinit var btnManagePlatforms: Button

    @FXML
    private lateinit var btnManagePublishers: Button

    @FXML
    private lateinit var btnManageItemType: Button

    @FXML
    private lateinit var btnVisitorLogDetails: Button

    @FXML
    fun initialize() {
        btnItemDetails.setOnAction { openTableView(Item::class) }
        btnLocationDetails.setOnAction { openTableView(Location::class) }
        btnVisitorDetails.setOnAction { openTableView(Visitor::class) }
        btnManageGenres.setOnAction { openTableView(Genre::class) }
        btnManagePlatforms.setOnAction { openTableView(Platform::class) }
        btnManagePublishers.setOnAction { openTableView(Publisher::class) }
        btnManageItemType.setOnAction { openTableView(ItemType::class) }
        btnVisitorLogDetails.setOnAction { openTableView(VisitorLog::class) }
    }

    private fun <T : DatabaseModel> openTableView(entityType: KClass<T>) {
        try {
            val stage = Stage()
            val loader = FXMLLoader(javaClass.classLoader.getResource("DataBaseModelTableView.fxml"))
            loader.setController(DatabaseModelTableView(entityType))
            val root: Parent = loader.load()

            val scene = Scene(root)

            stage.setScene(scene)
            stage.title = entityType.simpleName + " Table View"
            stage.initOwner(ProjectMain.rootStage)
            stage.initModality(Modality.WINDOW_MODAL)
            stage.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
