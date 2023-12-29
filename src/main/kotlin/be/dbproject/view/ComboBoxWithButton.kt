package be.dbproject.view

import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.layout.HBox
import javafx.util.StringConverter

class ComboBoxWithButton <T>() : HBox() {

    val comboBox : ComboBox<T>
        get() = field

    val button : Button
        get() = field

    init {
        comboBox = ComboBox<T>().apply {
            converter = object : StringConverter<T>() {
                override fun toString(entity: T?): String {
                    return entity?.let { entity.toString() } ?: "None"
                }

                override fun fromString(string: String?): T? {
                    return null
                }
            }
        }
        comboBox.prefWidthProperty().bind(widthProperty())

        button = Button("+").apply {
            minWidth = 25.0
            maxWidth = 25.0
        }

        children.add(comboBox)
        children.add(button)
    }
}