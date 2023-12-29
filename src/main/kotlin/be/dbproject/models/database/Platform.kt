package be.dbproject.models.database

import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "Platform")
data class Platform(
    @Column
    var name: String,

    @Column
    var description: String,

    @Column
    var releaseDate: LocalDate
) : DatabaseModel()
{
    constructor() : this (
        "",
        "",
        LocalDate.now()
    )

    override fun toString() : String{
        return name
    }
}
