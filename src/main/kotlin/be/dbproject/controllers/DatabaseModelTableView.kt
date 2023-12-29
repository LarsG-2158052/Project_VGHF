package be.dbproject.controllers

import be.dbproject.models.database.DatabaseModel
import be.dbproject.repositories.Repository
import be.dbproject.repositories.RepositoryException
import be.dbproject.view.DatabaseSearchBar
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.HBox
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

class DatabaseModelTableView<T : DatabaseModel>(private val entityClass: KClass<T>) {

    @FXML
    private lateinit var tableView: TableView<T>

    @FXML
    private lateinit var createElementBtn: Button

    @FXML
    private lateinit var deleteElementBtn: Button

    @FXML
    private lateinit var editElementBtn: Button

    @FXML
    private lateinit var filterBox: HBox

    private lateinit var searchBar : DatabaseSearchBar<T>

    @FXML
    fun initialize() {
        createElementBtn.setOnAction { openItemDialog() }
        deleteElementBtn.setOnAction { handleDeleteItem() }
        editElementBtn.setOnAction { handleEditItem() }

        initFilterBox()
        initTable()

        updateTable(Repository(entityClass).getAllEntities())
    }

    private fun initFilterBox() {
        searchBar = DatabaseSearchBar(entityClass).apply {
            prefWidthProperty().bind(filterBox.widthProperty())
            filterBox.children.add(this)
            setOnSearch { columnNames, columnTypes, searchParam, queryType ->

                val entities = Repository(entityClass).getEntitiesByColumn(
                    columnNames,
                    columnTypes,
                    searchParam,
                    queryType
                )

                updateTable(entities)
            }
        }
    }

    private fun initTable() {
        tableView.apply {
            selectionModel.selectionMode = SelectionMode.SINGLE
            setRowFactory {
                val row = TableRow<T>()
                row.setOnMouseClicked { event ->
                    if (event.clickCount == 2 && !row.isEmpty) {
                        handleEditItem()
                    }
                }
                row
            }
        }

        entityClass.primaryConstructor?.parameters?.forEach { parameter ->
            val col = TableColumn<T, Any>(parameter.name)
            col.setCellValueFactory { cellFeature ->
                val property = entityClass.declaredMemberProperties.find { it.name == parameter.name }
                ReadOnlyObjectWrapper(property?.get(cellFeature.value))
            }
            tableView.columns.add(col)
        }
    }

    private fun updateTable(entities: List<T>) {
        tableView.items.clear()
        tableView.items.addAll(entities)
    }

    private fun handleDeleteItem() {
        val selectedItem = tableView.selectionModel.selectedItem

        if (selectedItem != null) {
            Repository(entityClass).deleteEntity(selectedItem)
            tableView.items.remove(selectedItem)
        } else {
            val alert = Alert(Alert.AlertType.WARNING, "Select an item to delete.")
            alert.showAndWait()
        }
    }

    private fun handleEditItem() {
        val selectedItem = tableView.selectionModel.selectedItem

        if (selectedItem != null) {
            openItemDialog(selectedItem)
        } else {
            Alert(Alert.AlertType.WARNING, "Select an item to edit.").showAndWait()
        }
    }

    private fun openItemDialog(entity: T? = null) {
        EditDatabaseModelDialog(entityClass, tableView.scene.window, entity) { newEntity ->
            try {
                if (entity == null) {
                    Repository(entityClass).addEntity(newEntity)
                    tableView.items.add(newEntity)
                }
                else {
                    Repository(entityClass).updateEntity(entity)
                    tableView.refresh()
                }
            }
            catch (e : RepositoryException) {
                Alert(Alert.AlertType.ERROR).apply {
                    title = "Error while updating/adding ${entityClass.simpleName?.lowercase()}"
                    headerText = null
                    contentText = "An error occurred while attempting to update or add " +
                            "a ${entityClass.simpleName?.lowercase()}. " +
                            "Please ensure that all required unique fields are unique."
                    showAndWait()
                }
            }
        }
    }
}
