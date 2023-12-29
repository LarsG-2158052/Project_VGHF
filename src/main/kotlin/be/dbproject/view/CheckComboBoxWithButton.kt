package be.dbproject.view

import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.util.StringConverter
import org.controlsfx.control.CheckComboBox

class CheckComboBoxWithButton <T>() : HBox() {

    val checkComboBox : CheckComboBox<T>
        get() = field

    val button : Button
        get() = field

    init {
        prefWidth = Double.MAX_VALUE

        checkComboBox = CheckComboBox<T>().apply {
            converter = object : StringConverter<T>() {
                override fun toString(entity: T?): String {
                    return entity?.let { entity.toString() } ?: "None"
                }

                override fun fromString(string: String?): T? {
                    return null
                }
            }
        }
        checkComboBox.prefWidthProperty().bind(widthProperty())

        button = Button("+").apply {
            minWidth = 25.0
            maxWidth = 25.0
        }

        children.add(checkComboBox)
        children.add(button)
    }
}