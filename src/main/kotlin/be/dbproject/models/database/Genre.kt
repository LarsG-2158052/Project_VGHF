package be.dbproject.models.database

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "Genre")
data class Genre (
    @Column
    var name: String
) : DatabaseModel()
{
    constructor() : this (
        ""
    )

    override fun toString(): String {
        return name
    }
}
