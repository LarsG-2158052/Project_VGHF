package be.dbproject.models.database

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "LocationType")
data class LocationType(
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