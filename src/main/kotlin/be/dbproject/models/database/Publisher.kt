package be.dbproject.models.database

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "Publisher")
data class Publisher(
    @Column
    var name: String,

    @Column
    var description: String,

    @Column
    var website: String?
) : DatabaseModel()
{
    override fun toString(): String {
        return name
    }
}