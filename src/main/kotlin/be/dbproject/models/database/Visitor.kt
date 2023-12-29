package be.dbproject.models.database

import javax.persistence.*

@Entity
@Table(name = "Visitor")
data class Visitor(
    @Column
    var firstName: String,

    @Column
    var lastName: String,

    @Column
    var phone: String,

    @Column
    var email: String,

    @ManyToOne
    @JoinColumn(name = "locationID")
    var location: Location,
) : DatabaseModel()
{
    override fun toString(): String {
        return "$firstName $lastName"
    }
}