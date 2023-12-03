package be.dbproject.repositories

import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root


//Dit werkt opeens niet meer: fun <T, V> Root<T>.get(prop: KProperty1<T, V>): Path<V> = this.get(prop.name)
abstract class BaseRepository<T>(private val entityClass: Class<T>) {
    protected val entityManager: EntityManager = EntityManagerSingleton.instance

    fun addEntity(entity: T) {
        entityManager.transaction.begin()
        entityManager.persist(entity)
        entityManager.transaction.commit()
    }

    fun getAllEntities(): List<T> {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val query: CriteriaQuery<T> = criteriaBuilder.createQuery(entityClass)
        val root: Root<T> = query.from(entityClass)
        query.select(root)
        return entityManager.createQuery(query).resultList
    }

    fun getEntityById(entityId: Long): T? {
        return entityManager.find(entityClass, entityId)
    }

    fun updateEntity(entity: T) {
        withTransaction { entityManager.merge(entity) }
    }

    fun deleteEntity(entityId: Long) {
        entityManager.transaction.begin()
        val entity: T? = entityManager.find(entityClass, entityId)
        if (entity != null) {
            entityManager.remove(entity)
        }
        entityManager.transaction.commit()
    }

    private fun withTransaction(action: () -> Unit) {
        val transaction = entityManager.transaction
        try {
            transaction.begin()
            action()
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }
}