package be.dbproject.controllers

import be.dbproject.models.Genre
import be.dbproject.models.Item
import be.dbproject.models.ItemType
import be.dbproject.repositories.ItemRepository
import be.dbproject.repositories.GenreRepository
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import java.time.LocalDate
import java.util.*

class ItemsTableController {
    private lateinit var itemRepository: ItemRepository

    @FXML
    private lateinit var tblItems: TableView<Item>

    @FXML
    private lateinit var btn1: Button

    @FXML
    private lateinit var btn2: Button

    @FXML
    private lateinit var btn3: Button

    @FXML
    fun initialize() {
        btn1.setOnAction { handleNewItem() }
        btn2.setOnAction { handleDeleteItem() }
        btn3.setOnAction { handleEditItems() }


        this.itemRepository = ItemRepository()
        initTable()
    }

    private fun initTable() {
        tblItems.selectionModel.selectionMode = SelectionMode.SINGLE
        tblItems.columns.clear()

        val itemClass = Item::class.java
        val properties = itemClass.declaredFields

        for (property in properties) {
            val col = TableColumn<Item, Any>(property.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
            col.setCellValueFactory { cellData ->
                val field = itemClass.getDeclaredField(property.name)
                field.isAccessible = true
                ReadOnlyObjectWrapper(field.get(cellData.value))
            }
            tblItems.columns.add(col)
        }

        try {
            val items = itemRepository.getAllEntities()
            tblItems.items.addAll(items)
        } catch (e: Exception) {
            e.printStackTrace()
            val alert = Alert(Alert.AlertType.ERROR, "Error loading items.")
            alert.showAndWait()
        }
    }

    @FXML
    fun handleNewItem() = openItemDialog("Add Item", Item(0, 0, null, 0, null, "", 0.0, "", "", 0,LocalDate.now()))

    @FXML
    fun handleDeleteItem() {
        val selectedItem = tblItems.selectionModel.selectedItem

        if (selectedItem != null) {
            try {itemRepository.deleteEntity(selectedItem.id); tblItems.items.remove(selectedItem)}
            catch (e: Exception) {
                val alert = Alert(Alert.AlertType.ERROR, "Error deleting item.")
                alert.showAndWait()
            }
        } else {
            val alert = Alert(Alert.AlertType.WARNING, "Select an item to edit.")
            alert.showAndWait()
        }
    }

    @FXML
    fun handleEditItems() {
        val selectedItem = tblItems.selectionModel.selectedItem

        if (selectedItem != null) {
            openItemDialog("Edit Item", selectedItem)
        } else {
            val alert = Alert(Alert.AlertType.WARNING, "Select an item to edit.")
            alert.showAndWait()
        }
    }

    private fun openItemDialog(title: String, item: Item) {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("ItemInputDialog.fxml"))
        val dialogPane = fxmlLoader.load<VBox>()

        val dialogStage = Stage()
        dialogStage.title = title
        dialogStage.initModality(Modality.APPLICATION_MODAL)
        dialogStage.initOwner(tblItems.scene.window)
        dialogStage.scene = Scene(dialogPane)

        val nameTextField = fxmlLoader.namespace["nameTextField"] as TextField
        val typeComboBox = fxmlLoader.namespace["typeComboBox"] as ComboBox<ItemType>

        val genreComboBox = fxmlLoader.namespace["genreComboBox"] as ComboBox<Genre>
        genreComboBox.items.addAll(GenreRepository().getAllEntities())

        val priceTextField = fxmlLoader.namespace["priceTextField"] as TextField
        val descriptionTextField = fxmlLoader.namespace["descriptionTextField"] as TextField
        val seriesComboBox = fxmlLoader.namespace["seriesComboBox"] as ComboBox<String>
        val releaseDatePicker = fxmlLoader.namespace["releaseDatePicker"] as DatePicker

        nameTextField.text = item.name
        priceTextField.text = item.price.toString()
        descriptionTextField.text = item.description


        val okButton = fxmlLoader.namespace["okButton"] as Button
        val cancelButton = fxmlLoader.namespace["cancelButton"] as Button

        okButton.setOnAction {
            try {
                // TODO: geen String gebruiken om status aan te geven
                if (title == "Add Item") {
                    val newItem = Item(
                        name = nameTextField.text,
                        price = priceTextField.text.toDoubleOrNull() ?: 0.0,
                        description = descriptionTextField.text,
                        series = seriesComboBox.value.toString(),
                        releaseDate = releaseDatePicker.value,
                        locationId = 0,
                        platformId = 0,
                        publisherId = 0,
                        genreId = 0,
                        typeId = 0
                    )
                    itemRepository.addEntity(newItem)
                    val items = itemRepository.getAllEntities()
                    tblItems.items.setAll(items)
                } else if (title == "Edit Item") {
                    item.name = nameTextField.text
                    item.price = priceTextField.text.toDoubleOrNull() ?: 0.0
                    item.description = descriptionTextField.text
                    item.series = ""
                    //TODO: data wordt nu omgezet naar een string
                    item.releaseDate = releaseDatePicker.value

                    itemRepository.updateEntity(item)
                    tblItems.refresh()
                }

                dialogStage.close()
            } catch (e: Exception) {
                e.printStackTrace()
                val alert = Alert(Alert.AlertType.ERROR, "Error handling $title.")
                alert.showAndWait()
            }
        }

        cancelButton.setOnAction { dialogStage.close() }
        dialogStage.showAndWait()
    }
}

