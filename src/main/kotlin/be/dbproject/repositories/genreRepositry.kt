package be.dbproject.repositories
import be.dbproject.models.Genre
import be.dbproject.models.Item
import javax.persistence.Persistence
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Path
import javax.persistence.criteria.Root
import kotlin.reflect.KProperty1


class genreRepositry {
    private val entityManager = Persistence.createEntityManagerFactory("be.dbproject").createEntityManager()

    fun getGenreByName(name: String): List<Genre> {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val query: CriteriaQuery<Genre> = criteriaBuilder.createQuery(Genre::class.java)
        val root: Root<Genre>? = query.from(Genre::class.java)

        query.select(root)
        if (root != null) {
            query.where(criteriaBuilder.equal(root.get(Genre::name), name))
        }

        return entityManager.createQuery(query).resultList
    }

}