package be.dbproject.view

import be.dbproject.models.database.DatabaseModel
import be.dbproject.repositories.Repository
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf


class DatabaseSearchBar<T : DatabaseModel>(private val startClass : KClass<T>) : HBox() {

    private val inputFieldBox : HBox
    private val filterButtonHolder : HBox

    private var onSearch : ((
        columnNames: List<String>,
        columnType: List<KClass<*>>,
        searchParam: String,
        queryType: Repository.QueryType
    ) -> Unit)? = null

    inner class FilterMenu <V : Any> (kClass: KClass<V>) : ComboBox<KProperty1<V, *>>() {
        init {
            minWidth = 100.0
            maxWidth = 100.0

            converter = object : StringConverter<KProperty1<V, *>>() {
                override fun toString(property: KProperty1<V, *>?): String {
                    return property?.name ?: ""
                }

                override fun fromString(string: String?): KProperty1<V, *>? {
                    return null
                }
            }

            val classProperties = kClass.declaredMemberProperties

            classProperties.forEach {
                items.add(it)
            }

            setOnAction { createNewMenu(); search() }
        }

        fun createNewMenu() {
            if (filterButtonHolder.children.count() > 1) {
                val startIndex = filterButtonHolder.children.indexOf(this) + 1
                val endIndex = filterButtonHolder.children.lastIndex + 1
                filterButtonHolder.children.remove(startIndex, endIndex)
            }
            inputFieldBox.children.clear()

            val filterClass = value.returnType.classifier as KClass<*>

            if (filterClass.isSubclassOf(DatabaseModel::class)) {
                val newMenu = FilterMenu(filterClass)
                filterButtonHolder.children.add(newMenu)

                newMenu.apply {
                    if (items.count() == 1) {
                        value = items.first()
                        this.createNewMenu()
                    }
                }
            }
            else {
                val newMenu = FilterTypeMenu(filterClass)
                filterButtonHolder.children.add(newMenu)

                newMenu.apply {
                    if (items.count() == 1) {
                        value = items.first()
                        this.createNewInputField()
                    }
                }
            }
        }
    }

    inner class FilterTypeMenu <V : Any> (private val kClass: KClass<V>) : ComboBox<Repository.QueryType>() {
        init {
            minWidth = 100.0
            maxWidth = 100.0

            converter = object : StringConverter<Repository.QueryType>() {
                override fun toString(queryType: Repository.QueryType?): String {
                    queryType?.let {
                        return it.toString().lowercase().replace('_', ' ')
                    } ?: run {
                        return ""
                    }
                }

                override fun fromString(string: String?): Repository.QueryType? {
                    return null
                }
            }

            val options : List<Repository.QueryType> = if (kClass.isSubclassOf(DatabaseModel::class)) {
                listOf(Repository.QueryType.EQUAL)
            }
            else when (kClass) {
                String::class ->
                    listOf(Repository.QueryType.LIKE, Repository.QueryType.EQUAL)

                Int::class ->
                    listOf(Repository.QueryType.EQUAL, Repository.QueryType.LESS_THAN, Repository.QueryType.GREATER_THAN)

                Double::class ->
                    listOf(Repository.QueryType.EQUAL, Repository.QueryType.LESS_THAN, Repository.QueryType.GREATER_THAN)

                LocalDate::class ->
                    listOf(Repository.QueryType.EQUAL, Repository.QueryType.LESS_THAN, Repository.QueryType.GREATER_THAN)

                else -> throw IllegalArgumentException("Unsupported class: ${kClass.simpleName}")
            }

            setOnAction { createNewInputField(); search()}

            items.addAll(options)
        }

        fun createNewInputField() {
            val inputField = when (kClass) {
                LocalDate::class -> DatePicker().apply { setOnAction { search() } }
                else -> TextField().apply { textProperty().addListener { _, _, _ -> search() } }
            }

            inputFieldBox.apply {
                children.clear()
                children.add(inputField)
                setHgrow(inputField, Priority.ALWAYS)
            }
        }
    }

    init {
        alignment = Pos.CENTER

        filterButtonHolder = HBox()
        inputFieldBox = HBox()

        setHgrow(inputFieldBox, Priority.ALWAYS)
        setHgrow(filterButtonHolder, Priority.NEVER)

        children.apply {
            add(filterButtonHolder)
            add(inputFieldBox)
        }

        val mainFilterButton = FilterMenu(startClass)
        filterButtonHolder.children.add(mainFilterButton)
    }

    public fun setOnSearch(
        action : (
            columnNames: List<String>,
            columnTypes: List<KClass<*>>,
            searchParam: String,
            queryType: Repository.QueryType
        ) -> Unit) {
        onSearch = action
    }

    private fun search() {
        val columnNames : MutableList<String> = mutableListOf()
        val columnTypes : MutableList<KClass<*>> = mutableListOf()

        filterButtonHolder.children.dropLast(1).forEach {
            val menuValue = (it as FilterMenu<*>).value

            columnNames.add(menuValue.name)
            columnTypes.add(menuValue.returnType.classifier as KClass<*>)
        }

        val inputField = inputFieldBox.children.firstOrNull()
        inputField ?: run { onSearch?.invoke(emptyList(), emptyList(), "", Repository.QueryType.LIKE); return }

        val queryMenu = filterButtonHolder.children.lastOrNull() as? FilterTypeMenu<*>
        queryMenu ?: run { onSearch?.invoke(emptyList(), emptyList(), "", Repository.QueryType.LIKE); return }

        val searchString : String = when (inputField::class) {
            TextField::class -> (inputField as TextField).text
            DatePicker::class -> {
                val fieldValue = (inputField as DatePicker).value
                if(fieldValue != null) fieldValue.atStartOfDay(java.time.ZoneId.systemDefault())?.toEpochSecond().toString() else ""
            }
            else -> throw NotImplementedError()
        }

        val queryType : Repository.QueryType = queryMenu.value

        onSearch?.invoke(
            columnNames,
            columnTypes,
            searchString,
            queryType
        )
    }
}