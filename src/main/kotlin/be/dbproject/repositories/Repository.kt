package be.dbproject.repositories

import be.dbproject.models.database.DatabaseModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.persistence.EntityManager
import javax.persistence.criteria.*
import kotlin.reflect.KClass

// Source:  https://stackoverflow.com/questions/64521046/whats-the-correct-way-to-iterate-through-properties-of-a-singleton-in-kotlin
//          https://kotlinlang.org/docs/reflection.html
class Repository<T : DatabaseModel>(private val entityClass: KClass<T>) {
    private val entityManager: EntityManager = EntityManagerSingleton.instance

    enum class QueryType {
        LIKE, EQUAL, GREATER_THAN, LESS_THAN, RANGE
    }

    @Throws(RepositoryException::class)
    fun addEntity(entity: T) {
        withTransaction { entityManager.persist(entity) }
    }

    @Throws(RepositoryException::class)
    fun updateEntity(entity: T) {
        withTransaction { entityManager.merge(entity) }
    }

    @Throws(RepositoryException::class)
    fun deleteEntity(entity: T) {
        withTransaction {
            entityManager.find(entityClass.java, entity.id)?.let {
                entityManager.remove(it)
            }
        }
    }

    fun getAllEntities(): List<T> {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val query: CriteriaQuery<T> = criteriaBuilder.createQuery(entityClass.java)
        val root: Root<T> = query.from(entityClass.java)
        query.select(root)
        return entityManager.createQuery(query).resultList
    }

    fun getEntitiesByColumn(
        columNames: List<String>,
        columnTypes: List<KClass<*>>,
        searchParam: String,
        queryType: QueryType
    ): List<T> {

        if (columnTypes.isEmpty() || columNames.isEmpty() || searchParam.isBlank()) return getAllEntities()
        if (columnTypes.count() != columNames.count()) return listOf()

        // create query and root
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val query: CriteriaQuery<T> = criteriaBuilder.createQuery(entityClass.java)
        val root: Root<T> = query.from(entityClass.java)

        // create path to column
        var path : Path<*> = root
        for (i in columNames.dropLast(1).indices) {
            path = updatePath(path, columnTypes[i], columNames[i])
        }

        val predicate = createPredicate(queryType, path, columNames.last(), columnTypes.last(), searchParam)
        query.select(root).where(predicate)

        return entityManager.createQuery(query).resultList
    }

    private fun <V : Any> updatePath(path : Path<*>, kClass: KClass<V>, string : String) : Path<V> {
        return path.get(string)
    }

    private fun createPredicate(
        queryType : QueryType,
        path: Path<*>,
        columnName : String,
        columnType : KClass<*>,
        searchParam: String
    ) : Predicate {
        return when (queryType) {
            QueryType.LIKE ->
                createPredicateLike(columnName, searchParam, path, columnType)

            QueryType.EQUAL ->
                createPredicateEqual(columnName, searchParam, path, columnType)

            QueryType.GREATER_THAN ->
                createPredicateGreaterThen(columnName, searchParam, path, columnType)

            QueryType.LESS_THAN ->
                createPredicateLessThen(columnName, searchParam, path, columnType)

            QueryType.RANGE ->
                throw NotImplementedError()
        }
    }

    private fun createPredicateLike(columnName: String, searchParam: String, path: Path<*>, columnType: KClass<*>) : Predicate {

        if (columnType != String::class) return entityManager.criteriaBuilder.like(path.get(columnName), "%")
        else return entityManager.criteriaBuilder.like(path.get(columnName), "%$searchParam%")
    }

    private fun createPredicateEqual(columnName: String, searchParam: String, path: Path<*>, columnType: KClass<*>) : Predicate {
        val predicate : Predicate = when (columnType) {
            LocalDate::class -> entityManager.criteriaBuilder.equal(
                path.get<LocalDate>(columnName),
                LocalDate.ofInstant(Instant.ofEpochSecond(searchParam.toLong()), ZoneId.systemDefault())
            )
            else -> entityManager.criteriaBuilder.equal(path.get<String>(columnName), searchParam)
        }

        return predicate
    }

    private fun createPredicateGreaterThen(columnName: String, searchParam: String, path: Path<*>, columnType: KClass<*>) : Predicate {
        val predicate : Predicate = when (columnType) {
            LocalDate::class -> entityManager.criteriaBuilder.greaterThan(
                path.get(columnName),
                LocalDate.ofInstant(Instant.ofEpochSecond(searchParam.toLong()), ZoneId.systemDefault())
            )
            else -> entityManager.criteriaBuilder.greaterThan(path.get(columnName), searchParam)
        }

        return predicate
    }

    private fun createPredicateLessThen(columnName: String, searchParam: String, path: Path<*>, columnType: KClass<*>) : Predicate {
        val predicate : Predicate = when (columnType) {
            LocalDate::class -> entityManager.criteriaBuilder.lessThan(
                path.get(columnName),
                LocalDate.ofInstant(Instant.ofEpochSecond(searchParam.toLong()), ZoneId.systemDefault())
            )
            else -> entityManager.criteriaBuilder.lessThan(path.get(columnName), searchParam)
        }

        return predicate
    }

    @Throws(RepositoryException::class)
    private fun withTransaction(action: () -> Unit) {
        val transaction = entityManager.transaction
        try {
            transaction.begin()
            action()
            transaction.commit()
        }
        catch (e: Exception) {
            transaction.rollback()
            throw RepositoryException(e.message)
        }
    }
}
