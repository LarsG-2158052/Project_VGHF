package be.dbproject.models.database

import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class DatabaseModel(
    @Id
    @GeneratedValue
    val id: Long = 0,
)