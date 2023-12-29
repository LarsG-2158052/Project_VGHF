package be.dbproject.controllers

import be.dbproject.models.database.DatabaseModel
import be.dbproject.view.CheckComboBoxWithButton
import be.dbproject.view.ComboBoxWithButton
import be.dbproject.view.InputFieldFactory
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class EditDatabaseModelDialog<T : DatabaseModel>(
    private val entityClass: KClass<T>,
    private val owner : Window,
    private val entity : T? = null,
    private val onExit : (newElement :  T) -> Unit
) {

    private val stage = Stage()

    private val grid: GridPane
    private val okBtn: Button
    private val cancelBtn: Button

    init {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("EditDataBaseModelDialog.fxml"))
        val dialogPane = fxmlLoader.load<AnchorPane>()


        stage.title = when (entity){
            null -> "Create a new ${entityClass.simpleName?.lowercase()}"
            else -> "Edit ${entityClass.simpleName?.lowercase()}: $entity"
        }
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.initOwner(owner)
        stage.scene = Scene(dialogPane)

        grid = stage.scene.lookup("#grid") as GridPane
        okBtn = stage.scene.lookup("#okBtn") as Button
        cancelBtn = stage.scene.lookup("#cancelBtn") as Button

        okBtn.setOnAction { onOkeButton() }
        cancelBtn.setOnAction { stage.close() }

        // Source: https://stackoverflow.com/questions/51687098/kotlin-get-property-in-the-same-order-they-are-declared
        entityClass.primaryConstructor?.parameters?.forEach { parameter ->

            // get the value of the parameter
            val property = entityClass.declaredMemberProperties.first { it.name == parameter.name }
            val defaultValue = entity?.let {
                property.call(it)
            }

            // Check if parameter is a collection
            val isCollection : Boolean
            val inputFieldType : KClass<*>
            if ((parameter.type.classifier as KClass<*>).isSubclassOf(Collection::class)) {
                isCollection = true
                inputFieldType = parameter.type.arguments.first().type?.classifier as KClass<*>
            }
            else {
                isCollection = false
                inputFieldType = parameter.type.classifier as KClass<*>
            }

            // create the correct input field for the parameter type
            val inputField = InputFieldFactory().createInputFieldForClass(
                inputFieldType,
                parameter.type.isMarkedNullable,
                isCollection,
                defaultValue
            )

            // add input field to the view
            grid.add(Label(parameter.name), 0, parameter.index)
            grid.add(inputField, 1, parameter.index)
        }

        stage.showAndWait()
    }

    private fun onOkeButton() {
        val updatedEntity = updateEntityFromInputFields(entity)
        updatedEntity?.let {
            onExit(it)
            stage.close()
        }
    }

    private fun updateEntityFromInputFields(entity: T?): T? {

        val updatedEntity = when(entity) {
            null -> entityClass.createInstance()
            else -> entity
        }

        val valueList = getValuesFromInputFields()
        val errorList = mutableListOf<String>()

        // Source: https://stackoverflow.com/questions/44304480/how-to-set-delegated-property-value-by-reflection-in-kotlin
        entityClass.primaryConstructor!!.parameters.forEach { parameter ->
            val property = entityClass.declaredMemberProperties.first { it.name == parameter.name }
            if (property is KMutableProperty<*>) {
                try {
                    property.setter.call(updatedEntity, valueList[parameter.index])
                } catch (e: Exception) {
                    errorList.add("Invalid value for ${parameter.name}")
                }
            }
        }

        if (errorList.isNotEmpty()) {
            Alert(Alert.AlertType.ERROR).apply {
                title = null
                errorList.forEach { contentText += "\n$it" }

                showAndWait()
            }

            return null
        }
        return updatedEntity
    }

    private fun getValuesFromInputFields() : List<Any?> {

        val valueList : MutableList<Any?> = mutableListOf()

        entityClass.primaryConstructor!!.parameters.forEach { parameter ->
            val inputField =
                grid.children.find { GridPane.getRowIndex(it) == parameter.index && GridPane.getColumnIndex(it) == 1 }

            var value: Any? = null
            when (inputField!!::class) {
                TextField::class -> {
                    if ((inputField as TextField).text.isNotBlank()) {
                        when (parameter.type.classifier) {
                            Int::class -> value = inputField.text.toIntOrNull()
                            Double::class -> value = inputField.text.toDoubleOrNull()
                            String::class -> value = inputField.text
                        }
                    }
                }

                DatePicker::class -> value = (inputField as DatePicker).value
                ComboBoxWithButton::class -> value = (inputField as ComboBoxWithButton<*>).comboBox.value
                CheckComboBoxWithButton::class -> value =
                    (inputField as CheckComboBoxWithButton<*>).checkComboBox.checkModel.checkedItems.toHashSet()

                else -> throw IllegalArgumentException("Unsupported input field type: ${inputField::class.simpleName}")
            }

            valueList.add(value)
        }
        return valueList
    }
}